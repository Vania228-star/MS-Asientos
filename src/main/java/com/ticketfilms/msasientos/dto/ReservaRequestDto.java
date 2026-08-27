package com.ticketfilms.msasientos.dto;

import java.util.List;

import lombok.Data;

@Data
public class ReservaRequestDto {

    private String usuarioId;
    private Long funcionId;
    List<Long> asientosSolicitados;
}
