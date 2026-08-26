package com.ticketfilms.msasientos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketfilms.msasientos.model.Asiento;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long>{
    
    List<Asiento> findByFuncionId(Long funcionId);
}
