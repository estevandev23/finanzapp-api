package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.GastoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface GastoJpaRepository extends JpaRepository<GastoEntity, UUID> {

    List<GastoEntity> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);

    List<GastoEntity> findByUsuarioIdAndFechaBetweenOrderByFechaDesc(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    List<GastoEntity> findByUsuarioIdAndMesFacturacionOrderByFechaDesc(UUID usuarioId, LocalDate mesFacturacion);

    List<GastoEntity> findByUsuarioIdAndCategoriaOrderByFechaDesc(UUID usuarioId, CategoriaGasto categoria);

    @Query("SELECT COALESCE(SUM(g.monto), 0) FROM GastoEntity g WHERE g.usuarioId = :usuarioId")
    BigDecimal sumMontoByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT COALESCE(SUM(g.monto), 0) FROM GastoEntity g WHERE g.usuarioId = :usuarioId AND g.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal sumMontoByUsuarioIdAndFechaBetween(@Param("usuarioId") UUID usuarioId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(g.monto), 0) FROM GastoEntity g WHERE g.usuarioId = :usuarioId AND g.tarjetaId IS NOT NULL")
    BigDecimal sumMontoConTarjetaByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT COALESCE(SUM(g.monto), 0) FROM GastoEntity g WHERE g.usuarioId = :usuarioId AND g.tarjetaId IS NOT NULL AND g.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal sumMontoConTarjetaByUsuarioIdAndFechaBetween(@Param("usuarioId") UUID usuarioId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT g.categoria, SUM(g.monto) FROM GastoEntity g WHERE g.usuarioId = :usuarioId GROUP BY g.categoria")
    List<Object[]> sumMontoByUsuarioIdGroupByCategoria(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT g.categoria, SUM(g.monto) FROM GastoEntity g WHERE g.usuarioId = :usuarioId AND g.fecha BETWEEN :fechaInicio AND :fechaFin GROUP BY g.categoria")
    List<Object[]> sumMontoByUsuarioIdAndFechaBetweenGroupByCategoria(@Param("usuarioId") UUID usuarioId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Modifying
    @Query("DELETE FROM GastoEntity g WHERE g.deudaId = :deudaId")
    void deleteAllByDeudaId(@Param("deudaId") UUID deudaId);
}
