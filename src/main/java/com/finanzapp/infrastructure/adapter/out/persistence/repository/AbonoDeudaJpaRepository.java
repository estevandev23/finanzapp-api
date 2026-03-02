package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.AbonoDeudaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AbonoDeudaJpaRepository extends JpaRepository<AbonoDeudaEntity, UUID> {

    List<AbonoDeudaEntity> findByDeudaIdOrderByFechaAbonoDesc(UUID deudaId);

    Optional<AbonoDeudaEntity> findByGastoId(UUID gastoId);

    Optional<AbonoDeudaEntity> findByIngresoId(UUID ingresoId);

    @Modifying
    @Query("DELETE FROM AbonoDeudaEntity a WHERE a.deuda.id = :deudaId")
    void deleteAllByDeudaId(@Param("deudaId") UUID deudaId);
}
