package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.CategoriaPersonalizada;
import com.finanzapp.domain.model.TipoCategoria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaPersonalizadaRepositoryPort {
    CategoriaPersonalizada save(CategoriaPersonalizada categoria);
    Optional<CategoriaPersonalizada> findById(UUID id);
    List<CategoriaPersonalizada> findByUsuarioId(UUID usuarioId);
    List<CategoriaPersonalizada> findByUsuarioIdAndTipo(UUID usuarioId, TipoCategoria tipo);
    boolean existsByUsuarioIdAndNombreAndTipo(UUID usuarioId, String nombre, TipoCategoria tipo);
    void deleteById(UUID id);
}
