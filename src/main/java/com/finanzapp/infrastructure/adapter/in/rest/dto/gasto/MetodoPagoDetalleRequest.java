package com.finanzapp.infrastructure.adapter.in.rest.dto.gasto;

import com.finanzapp.domain.model.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoDetalleRequest {

    @NotNull(message = "El metodo de pago es requerido")
    private MetodoPago metodo;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;
}
