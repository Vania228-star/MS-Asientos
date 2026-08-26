package com.ticketfilms.msasientos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name= "Asientos")
@Data
public class Asiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "funcion_id", nullable = false)
    private Long funcionId;

    @Column(name= "numero_asiento", nullable = false)
    private String numeroAsiento;

    @Column(name= "estado", nullable = false)
    private String estado;
}
