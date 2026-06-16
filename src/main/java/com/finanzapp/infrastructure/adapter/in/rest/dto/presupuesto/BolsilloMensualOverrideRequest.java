package com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Override puntual de un bolsillo dentro de un mes ya generado. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BolsilloMensualOverrideRequest {

    @DecimalMin(value = "0.01")
    @DecimalMax(value = "100.00")
    private BigDecimal porcentaje;

    @DecimalMin(value = "0.01")
    private BigDecimal montoLimite;
}
