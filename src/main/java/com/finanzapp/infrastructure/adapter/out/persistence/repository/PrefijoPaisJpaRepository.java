package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.PrefijoPaisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PrefijoPaisJpaRepository extends JpaRepository<PrefijoPaisEntity, UUID> {

    List<PrefijoPaisEntity> findByActivoTrueOrderByNombreAsc();
}
