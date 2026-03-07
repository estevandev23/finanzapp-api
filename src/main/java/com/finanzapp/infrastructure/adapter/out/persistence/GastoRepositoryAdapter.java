package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.port.out.GastoRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.GastoEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.GastoMetodoPagoEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.GastoMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.GastoJpaRepository;
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
public class GastoRepositoryAdapter implements GastoRepositoryPort {

    private final GastoJpaRepository repository;
    private final GastoMapper mapper;

    @Override
    public Gasto save(Gasto gasto) {
        if (gasto.getId() != null) {
            Optional<GastoEntity> existente = repository.findById(gasto.getId());
            if (existente.isPresent()) {
                return actualizarEntidadManaged(existente.get(), gasto);
            }
        }
        var entity = mapper.toEntity(gasto);
        vincularMetodosPago(entity);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    private Gasto actualizarEntidadManaged(GastoEntity entity, Gasto gasto) {
        entity.setMonto(gasto.getMonto());
        entity.setCategoria(gasto.getCategoria());
        entity.setCategoriaPersonalizadaId(gasto.getCategoriaPersonalizadaId());
        entity.setDeudaId(gasto.getDeudaId());
        entity.setDescripcion(gasto.getDescripcion());
        entity.setFecha(gasto.getFecha());
        entity.setFechaActualizacion(gasto.getFechaActualizacion());

        if (gasto.getMetodosPago() != null) {
            entity.getMetodosPago().clear();
            for (var mp : gasto.getMetodosPago()) {
                entity.getMetodosPago().add(GastoMetodoPagoEntity.builder()
                        .id(mp.getId() != null ? mp.getId() : UUID.randomUUID())
                        .gasto(entity)
                        .metodo(mp.getMetodo())
                        .monto(mp.getMonto())
                        .build());
            }
        }

        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    private void vincularMetodosPago(GastoEntity entity) {
        if (entity.getMetodosPago() != null) {
            entity.getMetodosPago().forEach(mp -> mp.setGasto(entity));
        }
    }

    @Override
    public Optional<Gasto> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Gasto> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Gasto> findByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.findByUsuarioIdAndFechaBetweenOrderByFechaDesc(usuarioId, fechaInicio, fechaFin)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Gasto> findByUsuarioIdAndCategoria(UUID usuarioId, CategoriaGasto categoria) {
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
    public List<Object[]> sumMontoByUsuarioIdGroupByCategoria(UUID usuarioId) {
        return repository.sumMontoByUsuarioIdGroupByCategoria(usuarioId);
    }

    @Override
    public List<Object[]> sumMontoByUsuarioIdAndFechaBetweenGroupByCategoria(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.sumMontoByUsuarioIdAndFechaBetweenGroupByCategoria(usuarioId, fechaInicio, fechaFin);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAllByDeudaId(UUID deudaId) {
        repository.deleteAllByDeudaId(deudaId);
    }
}
