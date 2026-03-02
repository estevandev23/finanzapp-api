package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.AhorroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AhorroJpaRepository extends JpaRepository<AhorroEntity, UUID> {

    List<AhorroEntity> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);

    List<AhorroEntity> findByUsuarioIdAndFechaBetweenOrderByFechaDesc(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    List<AhorroEntity> findByMetaIdOrderByFechaDesc(UUID metaId);

    Optional<AhorroEntity> findByIngresoId(UUID ingresoId);

    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM AhorroEntity a WHERE a.usuarioId = :usuarioId")
    BigDecimal sumMontoByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM AhorroEntity a WHERE a.usuarioId = :usuarioId AND a.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal sumMontoByUsuarioIdAndFechaBetween(@Param("usuarioId") UUID usuarioId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM AhorroEntity a WHERE a.metaId = :metaId")
    BigDecimal sumMontoByMetaId(@Param("metaId") UUID metaId);
}
