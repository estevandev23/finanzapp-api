package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.domain.model.TipoRecurrencia;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.RecurrenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecurrenciaJpaRepository extends JpaRepository<RecurrenciaEntity, UUID> {

    List<RecurrenciaEntity> findByUsuarioIdOrderByProximaFechaAsc(UUID usuarioId);

    List<RecurrenciaEntity> findByUsuarioIdAndTipoOrderByProximaFechaAsc(UUID usuarioId, TipoRecurrencia tipo);

    List<RecurrenciaEntity> findByUsuarioIdAndActivaTrueOrderByProximaFechaAsc(UUID usuarioId);

    List<RecurrenciaEntity> findByActivaTrueAndProximaFechaBetweenOrderByProximaFechaAsc(
            LocalDate desde, LocalDate hasta);
}
