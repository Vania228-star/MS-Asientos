package com.ticketfilms.msasientos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketfilms.msasientos.model.Funcion_Asiento;

@Repository
public interface Funcion_AsientoRepository extends JpaRepository<Funcion_Asiento, Long> {
    
    List<Funcion_Asiento> findByFuncion_id(Long funcion_id);

    Optional<Funcion_Asiento> findByFuncion_idAndAsiento_id(Long funcion_id, Long asiento_id);

    List<Funcion_Asiento> findByUsuario_id(String usuario_id);
}
