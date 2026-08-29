package com.ticketfilms.msasientos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticketfilms.msasientos.model.Funcion_Asiento;
import com.ticketfilms.msasientos.repository.Funcion_AsientoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Funcion_AsientoService {
    
    private final Funcion_AsientoRepository funcion_AsientoRepository;

    public List<Funcion_Asiento> obtenerMapaFuncion_Asientos(Long funcion_id){
        return funcion_AsientoRepository.findByFuncion_id(funcion_id);
    }

    public boolean reservarAsientos(String usuario_id, Long funcion_id, List<Long> asientosIds){
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(5);

        for (Long asiento_id : asientosIds){
            Optional<Funcion_Asiento> optFuncionAsiento = funcion_AsientoRepository.findByFuncion_idAndAsiento_id(funcion_id, asiento_id);

            if(optFuncionAsiento.isPresent()){
                Funcion_Asiento funcion_Asiento = optFuncionAsiento.get();

                if ("DISPONIBLE".equals(funcion_Asiento.getEstado())){
                    funcion_Asiento.setEstado("RESERVADO");
                    funcion_Asiento.setUsuario_id(usuario_id);
                    funcion_Asiento.setReservado_hasta(expiracion);

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
}