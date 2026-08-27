package com.ticketfilms.msasientos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketfilms.msasientos.model.Sala;

@Repository
public interface SalaRepository extends JpaRepository< Sala, Long> {
    
    Sala findBySala_codigo(String sala_codigo);
}
