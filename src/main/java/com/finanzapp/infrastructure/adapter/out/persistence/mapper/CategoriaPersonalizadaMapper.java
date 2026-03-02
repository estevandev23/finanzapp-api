package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.CategoriaPersonalizada;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.CategoriaPersonalizadaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoriaPersonalizadaMapper {

    public CategoriaPersonalizada toDomain(CategoriaPersonalizadaEntity entity) {
        if (entity == null) return null;

        return CategoriaPersonalizada.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .nombre(entity.getNombre())
                .tipo(entity.getTipo())
                .color(entity.getColor())
                .icono(entity.getIcono())
                .activa(entity.isActiva())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public CategoriaPersonalizadaEntity toEntity(CategoriaPersonalizada domain) {
        if (domain == null) return null;

        return CategoriaPersonalizadaEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .nombre(domain.getNombre())
                .tipo(domain.getTipo())
                .color(domain.getColor())
                .icono(domain.getIcono())
                .activa(domain.isActiva())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
