package com.ticketfilms.msasientos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ticketfilms.msasientos.model.Asiento;
import com.ticketfilms.msasientos.repository.AsientoRepository;

@ExtendWith(MockitoExtension.class)
public class AsientoServiceTest {
    
    @Mock
    private AsientoRepository asientoRepository;

    @InjectMocks
    private AsientoService asientoService;

    @Test
    public void testObtenerAsientosPorSala(){
        Long salaId = 1L;
        Asiento asiento = new Asiento();
        asiento.setId(1L);
        asiento.setSala_id(salaId);
        asiento.setFila("A");
        asiento.setNumero(1);

        when(asientoRepository.findBySalaId(salaId)).thenReturn(List.of(asiento));

        List<Asiento> asientos = asientoService.obtenerAsientosPorSala(salaId);

        assertNotNull(asientos);
        assertEquals(1, asientos.size());
        assertEquals("A", asientos.get(0).getFila());
        verify(asientoRepository, times(1)).findBySalaId(salaId);
    }
}
