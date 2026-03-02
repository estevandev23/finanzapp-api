package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {
    Optional<UsuarioEntity> findByEmail(String email);
    Optional<UsuarioEntity> findByTelefono(String telefono);
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
    Optional<UsuarioEntity> findByOauthProviderAndOauthProviderId(String oauthProvider, String oauthProviderId);
}
