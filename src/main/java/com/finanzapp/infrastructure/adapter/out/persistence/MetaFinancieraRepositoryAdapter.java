package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.EstadoMeta;
import com.finanzapp.domain.model.MetaFinanciera;
import com.finanzapp.domain.port.out.MetaFinancieraRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.MetaFinancieraMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.MetaFinancieraJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MetaFinancieraRepositoryAdapter implements MetaFinancieraRepositoryPort {

    private final MetaFinancieraJpaRepository repository;
    private final MetaFinancieraMapper mapper;

    @Override
    public MetaFinanciera save(MetaFinanciera meta) {
        var entity = mapper.toEntity(meta);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<MetaFinanciera> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MetaFinanciera> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetaFinanciera> findByUsuarioIdAndEstado(UUID usuarioId, EstadoMeta estado) {
        return repository.findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(usuarioId, estado)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
