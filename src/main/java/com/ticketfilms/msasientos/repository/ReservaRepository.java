package com.ticketfilms.msasientos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketfilms.msasientos.model.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long>{
    
    List<Reserva> findByUsuarioId(String usuarioId);
}
