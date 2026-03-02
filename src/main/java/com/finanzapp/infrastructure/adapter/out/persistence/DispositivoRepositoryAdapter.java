package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.DispositivoMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.DispositivoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DispositivoRepositoryAdapter implements DispositivoRepositoryPort {

    private final DispositivoJpaRepository repository;
    private final DispositivoMapper mapper;

    @Override
    public Dispositivo save(Dispositivo dispositivo) {
        var entity = mapper.toEntity(dispositivo);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Dispositivo> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Dispositivo> findByNumeroWhatsapp(String numeroWhatsapp) {
        return repository.findByNumeroWhatsapp(numeroWhatsapp).map(mapper::toDomain);
    }

    @Override
    public Optional<Dispositivo> findByTokenDispositivo(String token) {
        return repository.findByTokenDispositivo(token).map(mapper::toDomain);
    }

    @Override
    public List<Dispositivo> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Dispositivo> findByUsuarioIdAndActivo(UUID usuarioId, boolean activo) {
        return repository.findByUsuarioIdAndActivo(usuarioId, activo)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNumeroWhatsappAndUsuarioId(String numeroWhatsapp, UUID usuarioId) {
        return repository.existsByNumeroWhatsappAndUsuarioId(numeroWhatsapp, usuarioId);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
