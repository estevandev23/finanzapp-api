package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.Ahorro;
import com.finanzapp.domain.port.out.AhorroRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.AhorroMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.AhorroJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AhorroRepositoryAdapter implements AhorroRepositoryPort {

    private final AhorroJpaRepository repository;
    private final AhorroMapper mapper;

    @Override
    public Ahorro save(Ahorro ahorro) {
        var entity = mapper.toEntity(ahorro);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Ahorro> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Ahorro> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ahorro> findByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.findByUsuarioIdAndFechaBetweenOrderByFechaDesc(usuarioId, fechaInicio, fechaFin)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ahorro> findByMetaId(UUID metaId) {
        return repository.findByMetaIdOrderByFechaDesc(metaId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Ahorro> findByIngresoId(UUID ingresoId) {
        return repository.findByIngresoId(ingresoId).map(mapper::toDomain);
    }

    @Override
    public BigDecimal sumMontoByUsuarioId(UUID usuarioId) {
        return repository.sumMontoByUsuarioId(usuarioId);
    }

    @Override
    public BigDecimal sumMontoByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.sumMontoByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin);
    }

    @Override
    public BigDecimal sumMontoByMetaId(UUID metaId) {
        return repository.sumMontoByMetaId(metaId);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
