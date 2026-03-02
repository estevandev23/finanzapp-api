package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.domain.model.TipoCategoria;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.CategoriaPersonalizadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoriaPersonalizadaJpaRepository extends JpaRepository<CategoriaPersonalizadaEntity, UUID> {
    List<CategoriaPersonalizadaEntity> findByUsuarioIdOrderByNombreAsc(UUID usuarioId);
    List<CategoriaPersonalizadaEntity> findByUsuarioIdAndTipoOrderByNombreAsc(UUID usuarioId, TipoCategoria tipo);
    boolean existsByUsuarioIdAndNombreAndTipo(UUID usuarioId, String nombre, TipoCategoria tipo);
}
