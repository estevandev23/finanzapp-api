package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.domain.model.EstadoInversion;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.InversionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InversionJpaRepository extends JpaRepository<InversionEntity, UUID> {
    List<InversionEntity> findByUsuarioIdOrderByFechaInversionDesc(UUID usuarioId);
    List<InversionEntity> findByUsuarioIdAndEstadoOrderByFechaInversionDesc(UUID usuarioId, EstadoInversion estado);
}
