package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.DispositivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DispositivoJpaRepository extends JpaRepository<DispositivoEntity, UUID> {

    Optional<DispositivoEntity> findByNumeroWhatsapp(String numeroWhatsapp);

    Optional<DispositivoEntity> findByTokenDispositivo(String token);

    List<DispositivoEntity> findByUsuarioId(UUID usuarioId);

    List<DispositivoEntity> findByUsuarioIdAndActivo(UUID usuarioId, boolean activo);

    boolean existsByNumeroWhatsappAndUsuarioId(String numeroWhatsapp, UUID usuarioId);
}
