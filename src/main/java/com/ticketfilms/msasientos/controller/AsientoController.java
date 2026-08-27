package com.ticketfilms.msasientos.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketfilms.msasientos.dto.ReservaRequestDto;
import com.ticketfilms.msasientos.model.Asiento;
import com.ticketfilms.msasientos.model.Reserva;
import com.ticketfilms.msasientos.service.AsientoService;
import com.ticketfilms.msasientos.service.ReservaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/asientos")
@RequiredArgsConstructor
public class AsientoController {
    
    private final AsientoService asientoService;
    private final ReservaService reservaService;

    @GetMapping("/mapa/{funcionId}")
    public ResponseEntity<List<Asiento>> obtenerMapa(@PathVariable Long funcionId){
        List<Asiento> mapa = asientoService.obtenerMapaAsientos(funcionId);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping("/reserva")
    public ResponseEntity<Reserva> reservaAsientos(@RequestBody ReservaRequestDto request){
        Reserva nuevaReserva = reservaService.reservaAsientos(
            request.getUsuarioId(),
            request.getFuncionId(),
            request.getAsientosSolicitados()
        );
        return ResponseEntity.ok(nuevaReserva);
    }

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<Reserva> consultaReserva(@PathVariable Long reservaId){
        Optional<Reserva> reversa = reservaService.consultarReserva(reservaId);
        return reversa.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reserva/{reservaId}")
    public ResponseEntity<Void> liberarReserva(@PathVariable Long reservaId){
        boolean eliminado = reservaService.liberarReserva(reservaId);
        if(eliminado){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
