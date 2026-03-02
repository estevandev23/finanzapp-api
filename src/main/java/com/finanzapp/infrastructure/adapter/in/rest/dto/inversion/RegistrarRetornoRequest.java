package com.finanzapp.infrastructure.adapter.in.rest.dto.inversion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RegistrarRetornoRequest {

    @NotNull(message = "El retorno real es requerido")
    @DecimalMin(value = "0.01", message = "El retorno debe ser mayor a 0")
    private BigDecimal retornoReal;

    private LocalDate fechaRetorno;
}
