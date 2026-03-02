package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.MetaFinanciera;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.MetaFinancieraEntity;
import org.springframework.stereotype.Component;

@Component
public class MetaFinancieraMapper {

    public MetaFinanciera toDomain(MetaFinancieraEntity entity) {
        if (entity == null) return null;

        return MetaFinanciera.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .montoObjetivo(entity.getMontoObjetivo())
                .montoActual(entity.getMontoActual())
                .fechaLimite(entity.getFechaLimite())
                .estado(entity.getEstado())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public MetaFinancieraEntity toEntity(MetaFinanciera domain) {
        if (domain == null) return null;

        return MetaFinancieraEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .nombre(domain.getNombre())
                .descripcion(domain.getDescripcion())
                .montoObjetivo(domain.getMontoObjetivo())
                .montoActual(domain.getMontoActual())
                .fechaLimite(domain.getFechaLimite())
                .estado(domain.getEstado())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
