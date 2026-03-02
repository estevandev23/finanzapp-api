package com.finanzapp.infrastructure.adapter.in.rest.dto.deuda;

import com.finanzapp.domain.model.TipoDeuda;
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
public class DeudaRequest {

    @NotNull(message = "El tipo es requerido")
    private TipoDeuda tipo;

    @NotBlank(message = "La descripcion es requerida")
    private String descripcion;

    private String entidad;

    @NotNull(message = "El monto total es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal montoTotal;

    private LocalDate fechaInicio;

    private LocalDate fechaLimite;
}
