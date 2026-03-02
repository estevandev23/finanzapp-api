package com.finanzapp.infrastructure.adapter.in.rest.dto.inversion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InversionRequest {

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    private BigDecimal retornoEsperado;

    private LocalDate fechaInversion;
}
