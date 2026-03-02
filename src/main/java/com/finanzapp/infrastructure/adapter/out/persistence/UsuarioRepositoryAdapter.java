package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.UsuarioMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repository;
    private final UsuarioMapper mapper;

    @Override
    public Usuario save(Usuario usuario) {
        var entity = mapper.toEntity(usuario);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByTelefono(String telefono) {
        return repository.findByTelefono(telefono).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByOAuthProviderAndProviderId(String provider, String providerId) {
        return repository.findByOauthProviderAndOauthProviderId(provider, providerId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        return repository.existsByTelefono(telefono);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
