package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.CategoriaPersonalizada;
import com.finanzapp.domain.model.TipoCategoria;
import com.finanzapp.domain.port.out.CategoriaPersonalizadaRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.CategoriaPersonalizadaMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.CategoriaPersonalizadaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoriaPersonalizadaRepositoryAdapter implements CategoriaPersonalizadaRepositoryPort {

    private final CategoriaPersonalizadaJpaRepository repository;
    private final CategoriaPersonalizadaMapper mapper;

    @Override
    public CategoriaPersonalizada save(CategoriaPersonalizada categoria) {
        var entity = mapper.toEntity(categoria);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CategoriaPersonalizada> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<CategoriaPersonalizada> findByUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByNombreAsc(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoriaPersonalizada> findByUsuarioIdAndTipo(UUID usuarioId, TipoCategoria tipo) {
        return repository.findByUsuarioIdAndTipoOrderByNombreAsc(usuarioId, tipo)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUsuarioIdAndNombreAndTipo(UUID usuarioId, String nombre, TipoCategoria tipo) {
        return repository.existsByUsuarioIdAndNombreAndTipo(usuarioId, nombre, tipo);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
