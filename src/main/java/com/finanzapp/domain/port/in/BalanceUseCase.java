package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.Balance;

import java.time.LocalDate;
import java.util.UUID;

public interface BalanceUseCase {
    Balance obtenerBalanceGeneral(UUID usuarioId);
    Balance obtenerBalancePorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
}
