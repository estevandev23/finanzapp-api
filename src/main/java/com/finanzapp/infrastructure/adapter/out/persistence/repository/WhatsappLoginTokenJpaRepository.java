package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.WhatsappLoginTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WhatsappLoginTokenJpaRepository extends JpaRepository<WhatsappLoginTokenEntity, UUID> {

    Optional<WhatsappLoginTokenEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM WhatsappLoginTokenEntity t WHERE t.fechaExpiracion < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
