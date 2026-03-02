package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.CodigoVerificacion;
import com.finanzapp.domain.model.TipoVerificacion;
import com.finanzapp.domain.port.out.CodigoVerificacionRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.CodigoVerificacionMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.CodigoVerificacionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CodigoVerificacionRepositoryAdapter implements CodigoVerificacionRepositoryPort {

    private final CodigoVerificacionJpaRepository repository;
    private final CodigoVerificacionMapper mapper;

    @Override
    public CodigoVerificacion save(CodigoVerificacion codigo) {
        var entity = mapper.toEntity(codigo);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CodigoVerificacion> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CodigoVerificacion> findByUsuarioIdAndCodigoAndTipoAndUsadoFalse(
            UUID usuarioId, String codigo, TipoVerificacion tipo) {
        return repository.findByUsuarioIdAndCodigoAndTipoAndUsadoFalse(usuarioId, codigo, tipo)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteExpiredCodes() {
        repository.deleteByFechaExpiracionBefore(LocalDateTime.now());
    }
}
