package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.IngresoEntity;
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
public interface IngresoJpaRepository extends JpaRepository<IngresoEntity, UUID> {

    List<IngresoEntity> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);

    List<IngresoEntity> findByUsuarioIdAndFechaBetweenOrderByFechaDesc(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    List<IngresoEntity> findByUsuarioIdAndCategoriaOrderByFechaDesc(UUID usuarioId, CategoriaIngreso categoria);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM IngresoEntity i WHERE i.usuarioId = :usuarioId")
    BigDecimal sumMontoByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM IngresoEntity i WHERE i.usuarioId = :usuarioId AND i.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal sumMontoByUsuarioIdAndFechaBetween(@Param("usuarioId") UUID usuarioId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(i.montoAhorro), 0) FROM IngresoEntity i WHERE i.usuarioId = :usuarioId AND i.montoAhorro > 0")
    BigDecimal sumMontoAhorroByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT COALESCE(SUM(i.montoAhorro), 0) FROM IngresoEntity i WHERE i.usuarioId = :usuarioId AND i.montoAhorro > 0 AND i.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal sumMontoAhorroByUsuarioIdAndFechaBetween(@Param("usuarioId") UUID usuarioId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Modifying
    @Query("DELETE FROM IngresoEntity i WHERE i.prestamoId = :prestamoId")
    void deleteAllByPrestamoId(@Param("prestamoId") UUID prestamoId);

    @Query("SELECT i.metodoPago, COALESCE(SUM(i.monto), 0) FROM IngresoEntity i WHERE i.usuarioId = :usuarioId GROUP BY i.metodoPago")
    List<Object[]> sumMontoByUsuarioIdGroupByMetodoPago(@Param("usuarioId") UUID usuarioId);
}
