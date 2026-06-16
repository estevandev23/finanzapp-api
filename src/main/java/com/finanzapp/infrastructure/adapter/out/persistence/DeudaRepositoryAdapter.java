package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.Deuda;
import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.TipoDeuda;
import com.finanzapp.domain.port.out.DeudaRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.DeudaMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.DeudaJpaRepository;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeudaRepositoryAdapter implements DeudaRepositoryPort {

    private final DeudaJpaRepository repository;
    private final UsuarioJpaRepository usuarioRepository;
    private final DeudaMapper mapper;

    @Override
    public Deuda save(Deuda deuda) {
        UsuarioEntity usuario = usuarioRepository.getReferenceById(deuda.getUsuarioId());
        var entity = mapper.toEntity(deuda, usuario);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Deuda> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Deuda> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<Deuda> findByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo) {
        return repository.findByUsuarioIdAndTipoOrderByFechaCreacionDesc(usuarioId, tipo).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<Deuda> findByUsuarioIdAndEstado(UUID usuarioId, EstadoDeuda estado) {
        return repository.findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(usuarioId, estado).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public BigDecimal sumMontoRestanteByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo) {
        return repository.sumMontoRestanteByUsuarioIdAndTipo(usuarioId, tipo);
    }

    @Override
    public BigDecimal sumMontoRestanteConTarjetaByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo) {
        return repository.sumMontoRestanteConTarjetaByUsuarioIdAndTipo(usuarioId, tipo);
    }

    @Override
    public BigDecimal sumMontoAbonadoByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo) {
        return repository.sumMontoAbonadoByUsuarioIdAndTipo(usuarioId, tipo);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
