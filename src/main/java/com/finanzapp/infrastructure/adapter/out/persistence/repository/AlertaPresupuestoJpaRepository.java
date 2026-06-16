package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.domain.model.NivelAlertaPresupuesto;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.AlertaPresupuestoEmitidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlertaPresupuestoJpaRepository extends JpaRepository<AlertaPresupuestoEmitidaEntity, UUID> {
    boolean existsByBolsilloMensualIdAndNivel(UUID bolsilloMensualId, NivelAlertaPresupuesto nivel);
}
