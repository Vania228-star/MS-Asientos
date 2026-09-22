package com.ticketfilms.msasientos.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketfilms.msasientos.client.FuncionClient;
import com.ticketfilms.msasientos.dto.FuncionResponse;
import com.ticketfilms.msasientos.exception.CarteleraNoDisponibleException;
import com.ticketfilms.msasientos.exception.FuncionNoEncontradaException;
import com.ticketfilms.msasientos.model.Asiento;
import com.ticketfilms.msasientos.model.Funcion_Asiento;
import com.ticketfilms.msasientos.model.Sala;
import com.ticketfilms.msasientos.repository.AsientoRepository;
import com.ticketfilms.msasientos.repository.Funcion_AsientoRepository;
import com.ticketfilms.msasientos.repository.SalaRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Funcion_AsientoService {

    private static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    private static final String ESTADO_RESERVADO = "RESERVADO";
    private static final String ESTADO_OCUPADO = "OCUPADO";
    private static final int MAX_ASIENTOS_POR_RESERVA = 10;

    private final Funcion_AsientoRepository funcion_AsientoRepository;
    private final AsientoRepository asientoRepository;
    private final SalaRepository salaRepository;
    private final FuncionClient funcionClient;

    private boolean estaExpirado(Funcion_Asiento fa) {
        return ESTADO_RESERVADO.equals(fa.getEstado())
                && fa.getReservado_hasta() != null
                && fa.getReservado_hasta().isBefore(LocalDateTime.now());
    }

    private boolean esReservaPropia(Funcion_Asiento fa, String usuario_id) {
        return ESTADO_RESERVADO.equals(fa.getEstado()) && usuario_id.equals(fa.getUsuario_id());
    }

    // Siempre se bloquean los asientos en el mismo orden (de menor a mayor id)
    // para que dos usuarios con asientos en común no se bloqueen entre sí (deadlock).
    private List<Long> ordenarSinRepetidos(List<Long> ids) {
        return ids.stream().distinct().sorted().toList();
    }

    public List<Funcion_Asiento> obtenerMapaFuncion_Asientos(Long funcion_id){
        List<Funcion_Asiento> asientos = funcion_AsientoRepository.findByFuncion_id(funcion_id);

        if (asientos.isEmpty()) {
            return generarMapaInicial(funcion_id);
        }

        List<Funcion_Asiento> expirados = asientos.stream()
                .filter(this::estaExpirado)
                .collect(Collectors.toList());

        if (!expirados.isEmpty()) {
            expirados.forEach(fa -> {
                fa.setEstado(ESTADO_DISPONIBLE);
                fa.setActualizado_en(LocalDateTime.now());
            });
            funcion_AsientoRepository.saveAll(expirados);
        }

        return asientos;
    }

    private Sala resolverSala(String salaValor) {
        Sala sala = salaRepository.findBySala_codigo(salaValor);
        if (sala != null) {
            return sala;
        }
        return salaRepository.findByNombre(salaValor)
                .orElseThrow(() -> new IllegalStateException(
                "No se encontró Sala para el valor '" + salaValor + "' (ni por código ni por nombre)"));
    }

    private List<Funcion_Asiento> generarMapaInicial(Long funcion_id) {
        FuncionResponse funcion;
        try {
            funcion = funcionClient.obtenerFuncionPorId(funcion_id);
        } catch (FeignException.NotFound e) {
            throw new FuncionNoEncontradaException(funcion_id);
        } catch (FeignException e) {
            throw new CarteleraNoDisponibleException(funcion_id, e);
        }

        Sala sala = resolverSala(funcion.getSala());
        List<Asiento> asientos = asientoRepository.findBySalaId(sala.getId());

        List<Funcion_Asiento> nuevos = asientos.stream()
                .map(asiento -> {
                    Funcion_Asiento fa = new Funcion_Asiento();
                    fa.setFuncion_id(funcion_id);
                    fa.setAsiento_id(asiento.getId());
                    fa.setEstado(ESTADO_DISPONIBLE);
                    fa.setActualizado_en(LocalDateTime.now());
                    return fa;
                })
                .toList();

        return funcion_AsientoRepository.saveAll(nuevos);
    }

    @Transactional
    public boolean reservarAsientos(String usuario_id, Long funcion_id, List<Long> asientosIds){
        if (asientosIds == null || asientosIds.isEmpty()) {
            return false;
        }

        if (ordenarSinRepetidos(asientosIds).size() > MAX_ASIENTOS_POR_RESERVA) {
            return false;
        }

        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(5);
        List<Funcion_Asiento> aReservar = new ArrayList<>();

        // Paso 1: bloquear y validar TODOS los asientos antes de modificar alguno
        for (Long asiento_id : ordenarSinRepetidos(asientosIds)) {
            Optional<Funcion_Asiento> optFuncionAsiento =
                    funcion_AsientoRepository.findByFuncion_idAndAsiento_idForUpdate(funcion_id, asiento_id);

            if (optFuncionAsiento.isEmpty()) {
                return false;
            }

            Funcion_Asiento funcion_Asiento = optFuncionAsiento.get();
            boolean libre = ESTADO_DISPONIBLE.equals(funcion_Asiento.getEstado())
                    || estaExpirado(funcion_Asiento);

            if (!libre && !esReservaPropia(funcion_Asiento, usuario_id)) {
                return false;
            }

            aReservar.add(funcion_Asiento);
        }

        // Paso 2: recién ahora se reserva todo junto
        for (Funcion_Asiento funcion_Asiento : aReservar) {
            funcion_Asiento.setEstado(ESTADO_RESERVADO);
            funcion_Asiento.setUsuario_id(usuario_id);
            funcion_Asiento.setReservado_hasta(expiracion);
            funcion_Asiento.setActualizado_en(LocalDateTime.now());
        }
        funcion_AsientoRepository.saveAll(aReservar);

        return true;
    }

    @Transactional
    public String confirmarAsientos(String usuario_id, Long funcion_id, List<Long> asientosIds){
        List<Funcion_Asiento> aConfirmar = new ArrayList<>();

        for (Long asiento_id : ordenarSinRepetidos(asientosIds)) {
            Optional<Funcion_Asiento> optFuncionAsiento =
                    funcion_AsientoRepository.findByFuncion_idAndAsiento_idForUpdate(funcion_id, asiento_id);

            if (optFuncionAsiento.isEmpty()) {
                return "ASIENTO_INEXISTENTE";
            }

            Funcion_Asiento funcion_Asiento = optFuncionAsiento.get();

            if (estaExpirado(funcion_Asiento)) {
                funcion_Asiento.setEstado(ESTADO_DISPONIBLE);
                funcion_Asiento.setActualizado_en(LocalDateTime.now());
                funcion_AsientoRepository.save(funcion_Asiento);
                return "RESERVA_EXPIRADA";
            }

            if (!ESTADO_RESERVADO.equals(funcion_Asiento.getEstado())) {
                return "ASIENTO_NO_RESERVADO";
            }

            if (!usuario_id.equals(funcion_Asiento.getUsuario_id())) {
                return "RESERVA_DE_OTRO_USUARIO";
            }

            aConfirmar.add(funcion_Asiento);
        }

        for (Funcion_Asiento funcion_Asiento : aConfirmar) {
            funcion_Asiento.setEstado(ESTADO_OCUPADO);
            funcion_Asiento.setActualizado_en(LocalDateTime.now());
        }
        funcion_AsientoRepository.saveAll(aConfirmar);

        return "OK";
    }

    @Transactional
    public String liberarAsientos(String usuario_id, Long funcion_id, List<Long> asientosIds){
        if (asientosIds == null || asientosIds.isEmpty()) {
            return "SOLICITUD_INVALIDA";
        }

        List<Funcion_Asiento> aLiberar = new ArrayList<>();

        for (Long asiento_id : ordenarSinRepetidos(asientosIds)) {
            Optional<Funcion_Asiento> optFuncionAsiento =
                    funcion_AsientoRepository.findByFuncion_idAndAsiento_idForUpdate(funcion_id, asiento_id);

            if (optFuncionAsiento.isEmpty()) {
                return "ASIENTO_INEXISTENTE";
            }

            Funcion_Asiento funcion_Asiento = optFuncionAsiento.get();

            if (!ESTADO_RESERVADO.equals(funcion_Asiento.getEstado())) {
                return "ASIENTO_NO_RESERVADO";
            }

            if (!estaExpirado(funcion_Asiento) && !usuario_id.equals(funcion_Asiento.getUsuario_id())) {
                return "RESERVA_DE_OTRO_USUARIO";
            }

            aLiberar.add(funcion_Asiento);
        }

        for (Funcion_Asiento funcion_Asiento : aLiberar) {
            funcion_Asiento.setEstado(ESTADO_DISPONIBLE);
            funcion_Asiento.setUsuario_id(null);
            funcion_Asiento.setReservado_hasta(null);
            funcion_Asiento.setActualizado_en(LocalDateTime.now());
        }
        funcion_AsientoRepository.saveAll(aLiberar);

        return "OK";
    }
}
