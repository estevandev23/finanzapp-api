package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.CodigoVerificacion;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.CodigoVerificacionEntity;
import org.springframework.stereotype.Component;

@Component
public class CodigoVerificacionMapper {

    public CodigoVerificacion toDomain(CodigoVerificacionEntity entity) {
        if (entity == null) return null;

        return CodigoVerificacion.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .codigo(entity.getCodigo())
                .tipo(entity.getTipo())
                .usado(entity.isUsado())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaExpiracion(entity.getFechaExpiracion())
                .build();
    }

    public CodigoVerificacionEntity toEntity(CodigoVerificacion domain) {
        if (domain == null) return null;

        return CodigoVerificacionEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .codigo(domain.getCodigo())
                .tipo(domain.getTipo())
                .usado(domain.isUsado())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaExpiracion(domain.getFechaExpiracion())
                .build();
    }
}
