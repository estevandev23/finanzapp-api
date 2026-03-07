package com.finanzapp.infrastructure.adapter.in.rest.dto.gasto;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GastoRequest {

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    private CategoriaGasto categoria;

    private UUID categoriaPersonalizadaId;

    private UUID deudaId;

    private String descripcion;

    private LocalDate fecha;

    private MetodoPago metodoPago;

    @Valid
    private List<MetodoPagoDetalleRequest> metodosPago;
}
