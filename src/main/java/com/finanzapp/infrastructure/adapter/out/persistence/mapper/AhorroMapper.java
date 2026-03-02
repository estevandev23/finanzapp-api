package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.Ahorro;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.AhorroEntity;
import org.springframework.stereotype.Component;

@Component
public class AhorroMapper {

    public Ahorro toDomain(AhorroEntity entity) {
        if (entity == null) return null;

        return Ahorro.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .ingresoId(entity.getIngresoId())
                .metaId(entity.getMetaId())
                .monto(entity.getMonto())
                .descripcion(entity.getDescripcion())
                .fecha(entity.getFecha())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public AhorroEntity toEntity(Ahorro domain) {
        if (domain == null) return null;

        return AhorroEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .ingresoId(domain.getIngresoId())
                .metaId(domain.getMetaId())
                .monto(domain.getMonto())
                .descripcion(domain.getDescripcion())
                .fecha(domain.getFecha())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
