package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositoryPort {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(UUID id);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByTelefono(String telefono);
    Optional<Usuario> findByOAuthProviderAndProviderId(String provider, String providerId);
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
    void deleteById(UUID id);
}
