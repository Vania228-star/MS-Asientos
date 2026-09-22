package com.ticketfilms.msasientos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name= "sala")
@Data
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    
    @Column(name= "sala_codigo", nullable = false)
    private String sala_codigo;

    @Column(name= "nombre", nullable = false)
    private String nombre;

    @Column(name="cantidad_filas", nullable = false)
    private Integer cantidad_filas;

    @Column(name="asientos_por_fila", nullable = false)
    private Integer asientos_por_fila;
}
