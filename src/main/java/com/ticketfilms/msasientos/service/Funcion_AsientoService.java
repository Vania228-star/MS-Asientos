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

    private final Funcion_AsientoRepository funcion_AsientoRepository;
    private final AsientoRepository asientoRepository;
    private final SalaRepository salaRepository;
    private final FuncionClient funcionClient;

    private boolean estaExpirado(Funcion_Asiento fa) {
        return ESTADO_RESERVADO.equals(fa.getEstado())
                && fa.getReservado_hasta() != null
                && fa.getReservado_hasta().isBefore(LocalDateTime.now());
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

    public boolean reservarAsientos(String usuario_id, Long funcion_id, List<Long> asientosIds){
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(5);

        for (Long asiento_id : asientosIds){
            Optional<Funcion_Asiento> optFuncionAsiento = funcion_AsientoRepository.findByFuncion_idAndAsiento_id(funcion_id, asiento_id);

            if(optFuncionAsiento.isPresent()){
                Funcion_Asiento funcion_Asiento = optFuncionAsiento.get();

                if (estaExpirado(funcion_Asiento)) {
                    funcion_Asiento.setEstado(ESTADO_DISPONIBLE);
                }

                if (ESTADO_DISPONIBLE.equals(funcion_Asiento.getEstado())){
                    funcion_Asiento.setEstado(ESTADO_RESERVADO);
                    funcion_Asiento.setUsuario_id(usuario_id);
                    funcion_Asiento.setReservado_hasta(expiracion);
                    funcion_Asiento.setActualizado_en(LocalDateTime.now());

                    funcion_AsientoRepository.save(funcion_Asiento);
                }else{
                    return false;
                }
            }else {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public String confirmarAsientos(String usuario_id, Long funcion_id, List<Long> asientosIds){
        List<Funcion_Asiento> aConfirmar = new ArrayList<>();

        for (Long asiento_id : asientosIds) {
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
}
