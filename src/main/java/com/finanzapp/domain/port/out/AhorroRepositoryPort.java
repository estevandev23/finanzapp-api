package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.Ahorro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AhorroRepositoryPort {
    Ahorro save(Ahorro ahorro);
    Optional<Ahorro> findById(UUID id);
    List<Ahorro> findByUsuarioId(UUID usuarioId);
    List<Ahorro> findByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    List<Ahorro> findByMetaId(UUID metaId);
    Optional<Ahorro> findByIngresoId(UUID ingresoId);
    BigDecimal sumMontoByUsuarioId(UUID usuarioId);
    BigDecimal sumMontoByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
    BigDecimal sumMontoByMetaId(UUID metaId);
    void deleteById(UUID id);
}
