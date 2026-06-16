package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.PresupuestoMensualEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PresupuestoMensualJpaRepository extends JpaRepository<PresupuestoMensualEntity, UUID> {
    Optional<PresupuestoMensualEntity> findByUsuarioIdAndAnioAndMes(UUID usuarioId, Integer anio, Integer mes);
    List<PresupuestoMensualEntity> findByUsuarioIdOrderByAnioDescMesDesc(UUID usuarioId);
}
