package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.Ingreso;
import com.finanzapp.domain.port.out.IngresoRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.IngresoMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.IngresoJpaRepository;
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
public class IngresoRepositoryAdapter implements IngresoRepositoryPort {

    private final IngresoJpaRepository repository;
    private final IngresoMapper mapper;

    @Override
    public Ingreso save(Ingreso ingreso) {
        var entity = mapper.toEntity(ingreso);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Ingreso> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Ingreso> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ingreso> findByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.findByUsuarioIdAndFechaBetweenOrderByFechaDesc(usuarioId, fechaInicio, fechaFin)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ingreso> findByUsuarioIdAndCategoria(UUID usuarioId, CategoriaIngreso categoria) {
        return repository.findByUsuarioIdAndCategoriaOrderByFechaDesc(usuarioId, categoria)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
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
    public BigDecimal sumMontoAhorroByUsuarioId(UUID usuarioId) {
        return repository.sumMontoAhorroByUsuarioId(usuarioId);
    }

    @Override
    public BigDecimal sumMontoAhorroByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.sumMontoAhorroByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAllByPrestamoId(UUID prestamoId) {
        repository.deleteAllByPrestamoId(prestamoId);
    }
}
