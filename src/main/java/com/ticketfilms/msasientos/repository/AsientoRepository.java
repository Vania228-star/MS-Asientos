package com.ticketfilms.msasientos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketfilms.msasientos.model.Asiento;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long>{
    
    @Query("SELECT a FROM Asiento a WHERE a.sala_id = :salaId")
    List<Asiento> findBySalaId(@Param("salaId") Long salaId);
}
