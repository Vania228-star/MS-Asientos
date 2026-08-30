package com.ticketfilms.msasientos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ticketfilms.msasientos.model.Sala;
import com.ticketfilms.msasientos.repository.SalaRepository;

@ExtendWith(MockitoExtension.class)
public class SalaServiceTest {
    
    @Mock
    private SalaRepository salaRepository;

    @InjectMocks
    private SalaService salaService;

    @Test
    public void testListarSala(){
        Sala sala = new Sala();
        sala.setId(1L);
        sala.setNombre("Sala Principal");
        sala.setSala_codigo("S01");

        when(salaRepository.findAll()).thenReturn(List.of(sala));

        List<Sala> salas = salaService.listarSalas();

        assertNotNull(salas);
        assertEquals(1, salas.size());
        assertEquals("Sala Principal", salas.get(0).getNombre());
        verify(salaRepository, times(1)).findAll();
    }

    @Test
    public void testObtenerSalaPorId(){
        Sala sala = new Sala();
        sala.setId(1L);
        sala.setNombre("Sala VIP");

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));

        Optional<Sala> resultado = salaService.obtenerSalaPorId(1L);
        
        assertTrue(resultado.isPresent());
        assertEquals("Sala VIP", resultado.get().getNombre());
    }
}
