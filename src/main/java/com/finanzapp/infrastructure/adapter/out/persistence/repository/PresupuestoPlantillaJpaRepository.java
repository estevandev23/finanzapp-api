package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.PresupuestoPlantillaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PresupuestoPlantillaJpaRepository extends JpaRepository<PresupuestoPlantillaEntity, UUID> {
    Optional<PresupuestoPlantillaEntity> findByUsuarioId(UUID usuarioId);
}
