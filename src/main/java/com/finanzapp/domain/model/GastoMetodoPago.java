package com.finanzapp.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoMetodoPago {
    private UUID id;
    private UUID gastoId;
    private MetodoPago metodo;
    private BigDecimal monto;
}
