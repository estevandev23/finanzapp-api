package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.SesionWhatsappEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionWhatsappJpaRepository extends JpaRepository<SesionWhatsappEntity, UUID> {

    Optional<SesionWhatsappEntity> findByNumeroWhatsapp(String numeroWhatsapp);

    Optional<SesionWhatsappEntity> findByNumeroWhatsappAndActivaTrue(String numeroWhatsapp);

    void deleteByNumeroWhatsapp(String numeroWhatsapp);
}
