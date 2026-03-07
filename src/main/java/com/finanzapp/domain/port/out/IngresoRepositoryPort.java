package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.Ingreso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngresoRepositoryPort {
    Ingreso save(Ingreso ingreso);
    Optional<Ingreso> findById(UUID id);
    List<Ingreso> findByUsuarioId(UUID usuarioId);
    List<Ingreso> findByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    List<Ingreso> findByUsuarioIdAndCategoria(UUID usuarioId, CategoriaIngreso categoria);
    BigDecimal sumMontoByUsuarioId(UUID usuarioId);
    BigDecimal sumMontoByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    BigDecimal sumMontoAhorroByUsuarioId(UUID usuarioId);
    BigDecimal sumMontoAhorroByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    void deleteById(UUID id);
    void deleteAllByPrestamoId(UUID prestamoId);
    List<Object[]> sumMontoByUsuarioIdGroupByMetodoPago(UUID usuarioId);
}
