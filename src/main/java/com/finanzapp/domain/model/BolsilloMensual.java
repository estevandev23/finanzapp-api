package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BolsilloMensual {
    private UUID id;
    private UUID presupuestoMensualId;
    private UUID bolsilloOrigenId;
    private String nombre;
    private TipoBolsillo tipo;
    private BigDecimal porcentaje;
    private BigDecimal montoLimite;
    private String color;
    private Integer orden;
}
