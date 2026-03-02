package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.domain.model.TipoVerificacion;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.CodigoVerificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoVerificacionJpaRepository extends JpaRepository<CodigoVerificacionEntity, UUID> {

    Optional<CodigoVerificacionEntity> findByUsuarioIdAndCodigoAndTipoAndUsadoFalse(
            UUID usuarioId, String codigo, TipoVerificacion tipo);

    @Modifying
    @Query("DELETE FROM CodigoVerificacionEntity c WHERE c.fechaExpiracion < :now")
    void deleteByFechaExpiracionBefore(LocalDateTime now);
}
