package com.ticketfilms.msasientos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticketfilms.msasientos.model.Reserva;
import com.ticketfilms.msasientos.repository.ReservaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservaService {
    
    private final ReservaRepository reservaRepository;

    public Reserva reservaAsientos(String usuarioId, Long funcionId, List<Long> asientosSolicitados){

        String asientosStr = asientosSolicitados.stream()
        .map(String::valueOf)
        .collect(java.util.stream.Collectors.joining(","));

        Reserva reserva = new Reserva();
        reserva.setUsuarioId(usuarioId);
        reserva.setFuncionId(funcionId);
        reserva.setAsientosIds(asientosStr);
        reserva.setEstado("ACTIVA");

        reserva.setFechaExpiracion(LocalDateTime.now().plusMinutes(5));
        return reservaRepository.save(reserva);

    }
    public Optional<Reserva> consultarReserva(Long reservaId){
        return reservaRepository.findById(reservaId);
    }

    public List<Reserva> listarReservasPorUsuario(String usuarioId){
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    public boolean liberarReserva(Long reservaId){
        if(reservaRepository.existsById(reservaId)){
            reservaRepository.deleteById(reservaId);
            return true;
        }
        return false;
    }
}
