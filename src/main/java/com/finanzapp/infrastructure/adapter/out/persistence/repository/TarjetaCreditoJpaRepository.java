package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.TarjetaCreditoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TarjetaCreditoJpaRepository extends JpaRepository<TarjetaCreditoEntity, UUID> {

    List<TarjetaCreditoEntity> findByUsuarioIdOrderByNombreAsc(UUID usuarioId);
}
