package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.Ahorro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AhorroUseCase {
    Ahorro registrar(Ahorro ahorro);
    Ahorro obtenerPorId(UUID id);
    List<Ahorro> listarPorUsuario(UUID usuarioId);
    List<Ahorro> listarPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    List<Ahorro> listarPorMeta(UUID metaId);
    BigDecimal obtenerTotalAhorros(UUID usuarioId);
    BigDecimal obtenerTotalAhorrosPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    Ahorro actualizar(UUID id, Ahorro ahorro);
    Optional<Ahorro> buscarPorIngresoId(UUID ingresoId);
    void eliminar(UUID id);
}
