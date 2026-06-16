package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.TipoDeuda;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.DeudaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeudaJpaRepository extends JpaRepository<DeudaEntity, UUID> {

    List<DeudaEntity> findByUsuarioIdOrderByFechaCreacionDesc(UUID usuarioId);

    List<DeudaEntity> findByUsuarioIdAndTipoOrderByFechaCreacionDesc(UUID usuarioId, TipoDeuda tipo);

    List<DeudaEntity> findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(UUID usuarioId, EstadoDeuda estado);

    @Query("SELECT SUM(d.montoRestante) FROM DeudaEntity d WHERE d.usuario.id = :usuarioId AND d.tipo = :tipo AND d.estado != 'COMPLETADA'")
    BigDecimal sumMontoRestanteByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo);

    @Query("SELECT COALESCE(SUM(d.montoRestante), 0) FROM DeudaEntity d WHERE d.usuario.id = :usuarioId AND d.tipo = :tipo AND d.estado != 'COMPLETADA' AND d.tarjetaId IS NOT NULL")
    BigDecimal sumMontoRestanteConTarjetaByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo);

    @Query("SELECT SUM(d.montoAbonado) FROM DeudaEntity d WHERE d.usuario.id = :usuarioId AND d.tipo = :tipo")
    BigDecimal sumMontoAbonadoByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo);
}
