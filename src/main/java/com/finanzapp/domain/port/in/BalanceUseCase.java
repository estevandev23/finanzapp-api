package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.Balance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface BalanceUseCase {
    Balance obtenerBalanceGeneral(UUID usuarioId);
    Balance obtenerBalancePorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    Map<String, BigDecimal[]> obtenerBalancePorMetodoPago(UUID usuarioId);
}
