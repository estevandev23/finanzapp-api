package com.finanzapp.infrastructure.adapter.in.rest.dto.meta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaFinancieraRequest {

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El monto objetivo es requerido")
    @DecimalMin(value = "0.01", message = "El monto objetivo debe ser mayor a 0")
    private BigDecimal montoObjetivo;

    private LocalDate fechaLimite;
}
