package com.ticketfilms.msasientos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name= "Reservas")
@Data
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "usuario_id", nullable = false)
    private String usuarioId;

    @Column(name= "funcion_id", nullable = false)
    private Long funcionId;

    @Column(name= "asiento_ids", nullable = false)
    private String asientosIds;

    @Column(name= "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name= "estado", nullable = false)
    private String estado;
}
