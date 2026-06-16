package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Gasto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GastoRepositoryPort {
    Gasto save(Gasto gasto);
    Optional<Gasto> findById(UUID id);
    List<Gasto> findByUsuarioId(UUID usuarioId);
    List<Gasto> findByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    List<Gasto> findByUsuarioIdAndMesFacturacion(UUID usuarioId, LocalDate mesFacturacion);
    List<Gasto> findByUsuarioIdAndCategoria(UUID usuarioId, CategoriaGasto categoria);
    BigDecimal sumMontoByUsuarioId(UUID usuarioId);
    BigDecimal sumMontoByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    BigDecimal sumMontoConTarjetaByUsuarioId(UUID usuarioId);
    BigDecimal sumMontoConTarjetaByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    List<Object[]> sumMontoByUsuarioIdGroupByCategoria(UUID usuarioId);
    List<Object[]> sumMontoByUsuarioIdAndFechaBetweenGroupByCategoria(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    void deleteById(UUID id);
    void deleteAllByDeudaId(UUID deudaId);
}
