package com.ticketfilms.msasientos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ticketfilms.msasientos.model.Asiento;
import com.ticketfilms.msasientos.repository.AsientoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsientoService {

    private final AsientoRepository asientoRepository;

    public List<Asiento> obtenerMapaAsientos(Long funcionId){
        return asientoRepository.findByFuncionId(funcionId);
    }

    public Asiento actualizarEstadoAsiento(Long asientoId, String nuevoEstado){
        Asiento asiento = asientoRepository.findById(asientoId)
                .orElseThrow(() -> new RuntimeException("Asiento no encontrado con ID: " + asientoId));
        asiento.setEstado(nuevoEstado);
        return asientoRepository.save(asiento);
    }
}
