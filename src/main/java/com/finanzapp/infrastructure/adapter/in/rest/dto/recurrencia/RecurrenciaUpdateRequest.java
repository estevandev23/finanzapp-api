package com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.FrecuenciaRecurrencia;
import com.finanzapp.domain.model.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurrenciaUpdateRequest {

    @Size(max = 255)
    private String descripcion;

    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    private FrecuenciaRecurrencia frecuencia;
    private CategoriaIngreso categoriaIngreso;
    private CategoriaGasto categoriaGasto;
    private UUID categoriaPersonalizadaId;
    private MetodoPago metodoPago;

    private UUID tarjetaId;

    private UUID bolsilloId;

    @Min(value = 1) @Max(value = 31)
    private Integer diaVencimiento;

    @Min(value = 1) @Max(value = 12)
    private Integer mesReferencia;

    private LocalDate proximaFecha;
}
