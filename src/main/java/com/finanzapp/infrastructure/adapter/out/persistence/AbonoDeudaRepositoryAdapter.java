package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.AbonoDeuda;
import com.finanzapp.domain.port.out.AbonoDeudaRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.DeudaEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.AbonoDeudaMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.AbonoDeudaJpaRepository;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.DeudaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AbonoDeudaRepositoryAdapter implements AbonoDeudaRepositoryPort {

    private final AbonoDeudaJpaRepository repository;
    private final DeudaJpaRepository deudaRepository;
    private final AbonoDeudaMapper mapper;

    @Override
    public AbonoDeuda save(AbonoDeuda abono) {
        DeudaEntity deuda = deudaRepository.getReferenceById(abono.getDeudaId());
        var entity = mapper.toEntity(abono, deuda);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<AbonoDeuda> findByDeudaId(UUID deudaId) {
        return repository.findByDeudaIdOrderByFechaAbonoDesc(deudaId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public Optional<AbonoDeuda> findByGastoId(UUID gastoId) {
        return repository.findByGastoId(gastoId).map(mapper::toDomain);
    }

    @Override
    public Optional<AbonoDeuda> findByIngresoId(UUID ingresoId) {
        return repository.findByIngresoId(ingresoId).map(mapper::toDomain);
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
