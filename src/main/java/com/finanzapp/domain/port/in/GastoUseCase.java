package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Gasto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GastoUseCase {
    Gasto registrar(Gasto gasto);

    /**
     * Registra el gasto sin aumentar el cupo usado de la tarjeta de crédito. Se usa cuando
     * el consumo del cupo se gestiona en otra entidad (por ejemplo, una deuda asociada a la
     * compra), para evitar contar el mismo movimiento dos veces contra la tarjeta.
     */
    Gasto registrarSinAfectarCupo(Gasto gasto);

    Gasto obtenerPorId(UUID id);
    List<Gasto> listarPorUsuario(UUID usuarioId);
    List<Gasto> listarPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    List<Gasto> listarPorCategoria(UUID usuarioId, CategoriaGasto categoria);
    BigDecimal obtenerTotalGastos(UUID usuarioId);
    BigDecimal obtenerTotalGastosPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    Map<CategoriaGasto, BigDecimal> obtenerDesglosePorCategoria(UUID usuarioId);
    Map<CategoriaGasto, BigDecimal> obtenerDesglosePorCategoriaPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    Gasto actualizar(UUID id, Gasto gasto);
    void eliminar(UUID id);
}
