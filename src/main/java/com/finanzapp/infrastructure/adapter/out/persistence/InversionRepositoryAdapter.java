package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.EstadoInversion;
import com.finanzapp.domain.model.Inversion;
import com.finanzapp.domain.port.out.InversionRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.InversionMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.InversionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InversionRepositoryAdapter implements InversionRepositoryPort {

    private final InversionJpaRepository repository;
    private final InversionMapper mapper;

    @Override
    public Inversion save(Inversion inversion) {
        return mapper.toDomain(repository.save(mapper.toEntity(inversion)));
    }

    @Override
    public Optional<Inversion> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Inversion> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByFechaInversionDesc(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Inversion> findByUsuarioIdAndEstado(UUID usuarioId, EstadoInversion estado) {
        return repository.findByUsuarioIdAndEstadoOrderByFechaInversionDesc(usuarioId, estado)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
