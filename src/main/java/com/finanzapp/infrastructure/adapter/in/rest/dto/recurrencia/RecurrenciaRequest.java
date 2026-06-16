package com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.FrecuenciaRecurrencia;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.TipoRecurrencia;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RecurrenciaRequest {

    @NotNull(message = "El tipo es requerido (INGRESO o GASTO)")
    private TipoRecurrencia tipo;

    @NotNull(message = "La frecuencia es requerida")
    private FrecuenciaRecurrencia frecuencia;

    @NotBlank(message = "La descripción es requerida")
    @Size(max = 255)
    private String descripcion;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    private CategoriaIngreso categoriaIngreso;
    private CategoriaGasto categoriaGasto;
    private UUID categoriaPersonalizadaId;

    private MetodoPago metodoPago;

    private UUID tarjetaId;

    private UUID bolsilloId;

    @Min(value = 1, message = "diaVencimiento debe estar entre 1 y 31")
    @Max(value = 31, message = "diaVencimiento debe estar entre 1 y 31")
    private int diaVencimiento;

    @Min(value = 1, message = "mesReferencia debe estar entre 1 y 12")
    @Max(value = 12, message = "mesReferencia debe estar entre 1 y 12")
    private Integer mesReferencia;

    private LocalDate proximaFecha;
}
