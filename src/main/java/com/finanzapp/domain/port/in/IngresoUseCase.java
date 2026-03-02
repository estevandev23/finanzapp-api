package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.Ingreso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IngresoUseCase {
    Ingreso registrar(Ingreso ingreso);
    Ingreso obtenerPorId(UUID id);
    List<Ingreso> listarPorUsuario(UUID usuarioId);
    List<Ingreso> listarPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    List<Ingreso> listarPorCategoria(UUID usuarioId, CategoriaIngreso categoria);
    BigDecimal obtenerTotalIngresos(UUID usuarioId);
    BigDecimal obtenerTotalIngresosPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    Ingreso actualizar(UUID id, Ingreso ingreso);
    void eliminar(UUID id);
}
