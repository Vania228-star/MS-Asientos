package com.ticketfilms.msasientos.controller;

import java.util.List;

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
import com.ticketfilms.msasientos.model.Funcion_Asiento;
import com.ticketfilms.msasientos.model.Sala;
import com.ticketfilms.msasientos.service.AsientoService;
import com.ticketfilms.msasientos.service.Funcion_AsientoService;
import com.ticketfilms.msasientos.service.SalaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/asientos")
@RequiredArgsConstructor
public class AsientoController {
    
    private final AsientoService asientoService;
    private final SalaService salaService;
    private final Funcion_AsientoService funcion_AsientoService;

    @GetMapping("/sala")
    public ResponseEntity<List<Sala>> listarSalas(){
        return ResponseEntity.ok(salaService.listarSalas());
    }

    @GetMapping("/sala/{salaId}")
    public ResponseEntity<List<Asiento>> obtenerAsientosPorSala(@PathVariable Long salaId){
        return ResponseEntity.ok(asientoService.obtenerAsientosPorSala(salaId));
    }

    @GetMapping("/mapa/{funcionId}")
    public ResponseEntity<List<Funcion_Asiento>> obtenerMapaFuncion(@PathVariable Long funcionId){
        List<Funcion_Asiento> mapa = funcion_AsientoService.obtenerMapaFuncion_Asientos(funcionId);
        return ResponseEntity.ok(mapa);
    }

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<String> consultarReserva(@PathVariable Long reservaId){
        return ResponseEntity.ok("Estado de la reserva...");
    }

    @DeleteMapping("/reserva/{reservaId}")
    public ResponseEntity<Void> liberarReserva(@PathVariable Long reservaId){
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reserva")
    public ResponseEntity<String> reservaAsientos(@RequestBody ReservaRequestDto request){
        boolean exito = funcion_AsientoService.reservarAsientos(
            request.getUsuarioId(),
            request.getFuncionId(),
            request.getAsientosSolicitados()
        );
        if (exito) {
            return ResponseEntity.ok("Asientos reservados temporalmente con éxito");
        }else{
            return ResponseEntity.badRequest().body("No pudo completarse esta reserva. Algunos asientos ya no se encuentran disponibles");
        }
    }
}
