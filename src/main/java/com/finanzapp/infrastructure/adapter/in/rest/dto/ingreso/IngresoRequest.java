package com.finanzapp.infrastructure.adapter.in.rest.dto.ingreso;

import com.finanzapp.domain.model.CategoriaIngreso;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngresoRequest {

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    private CategoriaIngreso categoria;

    private UUID categoriaPersonalizadaId;

    private String descripcion;

    private LocalDate fecha;

    @DecimalMin(value = "0", message = "El monto de ahorro no puede ser negativo")
    private BigDecimal montoAhorro;

    private UUID metaId;

    private UUID prestamoId;
}
