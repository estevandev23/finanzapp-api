package com.finanzapp.infrastructure.adapter.in.rest.dto.deuda;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeudaUpdateRequest {

    private String descripcion;

    private String entidad;

    private String categoria;

    private String categoriaPersonalizadaId;

    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal montoTotal;

    private LocalDate fechaLimite;
}
