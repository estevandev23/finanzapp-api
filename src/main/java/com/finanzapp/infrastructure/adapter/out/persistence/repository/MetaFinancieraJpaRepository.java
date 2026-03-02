package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.domain.model.EstadoMeta;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.MetaFinancieraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MetaFinancieraJpaRepository extends JpaRepository<MetaFinancieraEntity, UUID> {

    List<MetaFinancieraEntity> findByUsuarioIdOrderByFechaCreacionDesc(UUID usuarioId);

    List<MetaFinancieraEntity> findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(UUID usuarioId, EstadoMeta estado);
}
