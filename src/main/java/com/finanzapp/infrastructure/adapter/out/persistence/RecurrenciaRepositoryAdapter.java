package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoRecurrencia;
import com.finanzapp.domain.port.out.RecurrenciaRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.RecurrenciaEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.RecurrenciaMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.RecurrenciaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecurrenciaRepositoryAdapter implements RecurrenciaRepositoryPort {

    private final RecurrenciaJpaRepository repository;
    private final RecurrenciaMapper mapper;

    @Override
    public Recurrencia save(Recurrencia recurrencia) {
        if (recurrencia.getId() != null) {
            Optional<RecurrenciaEntity> existente = repository.findById(recurrencia.getId());
            if (existente.isPresent()) {
                return actualizarManaged(existente.get(), recurrencia);
            }
        }
        RecurrenciaEntity entity = mapper.toEntity(recurrencia);
        return mapper.toDomain(repository.save(entity));
    }

    private Recurrencia actualizarManaged(RecurrenciaEntity entity, Recurrencia recurrencia) {
        entity.setTipo(recurrencia.getTipo());
        entity.setFrecuencia(recurrencia.getFrecuencia());
        entity.setDescripcion(recurrencia.getDescripcion());
        entity.setMonto(recurrencia.getMonto());
        entity.setCategoriaIngreso(recurrencia.getCategoriaIngreso());
        entity.setCategoriaGasto(recurrencia.getCategoriaGasto());
        entity.setCategoriaPersonalizadaId(recurrencia.getCategoriaPersonalizadaId());
        entity.setMetodoPago(recurrencia.getMetodoPago());
        entity.setTarjetaId(recurrencia.getTarjetaId());
        entity.setBolsilloId(recurrencia.getBolsilloId());
        entity.setDiaVencimiento(recurrencia.getDiaVencimiento());
        entity.setMesReferencia(recurrencia.getMesReferencia());
        entity.setProximaFecha(recurrencia.getProximaFecha());
        entity.setUltimaConfirmacionFecha(recurrencia.getUltimaConfirmacionFecha());
        entity.setActiva(recurrencia.isActiva());
        entity.setFechaActualizacion(recurrencia.getFechaActualizacion());
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Recurrencia> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Recurrencia> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByProximaFechaAsc(usuarioId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Recurrencia> findByUsuarioIdAndTipo(UUID usuarioId, TipoRecurrencia tipo) {
        return repository.findByUsuarioIdAndTipoOrderByProximaFechaAsc(usuarioId, tipo)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Recurrencia> findActivasByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioIdAndActivaTrueOrderByProximaFechaAsc(usuarioId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Recurrencia> findActivasEnRangoProximaFecha(LocalDate desde, LocalDate hasta) {
        return repository.findByActivaTrueAndProximaFechaBetweenOrderByProximaFechaAsc(desde, hasta)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
