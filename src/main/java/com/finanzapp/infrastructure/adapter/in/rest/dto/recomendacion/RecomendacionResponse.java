package com.finanzapp.infrastructure.adapter.in.rest.dto.recomendacion;

import com.finanzapp.application.service.RecomendacionService.RecomendacionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionResponse {
    private String recomendacion;
    private LocalDateTime generadaEn;
    private int regeneracionesRestantes;
    private LocalDateTime proximaRegeneracionAutomatica;

    public static RecomendacionResponse from(RecomendacionResult result) {
        return RecomendacionResponse.builder()
                .recomendacion(result.recomendacion())
                .generadaEn(result.generadaEn())
                .regeneracionesRestantes(result.regeneracionesRestantes())
                .proximaRegeneracionAutomatica(result.proximaRegeneracionAutomatica())
                .build();
    }
}
