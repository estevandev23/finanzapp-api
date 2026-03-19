package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.SesionWhatsapp;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.SesionWhatsappEntity;
import org.springframework.stereotype.Component;

@Component
public class SesionWhatsappMapper {

    public SesionWhatsapp toDomain(SesionWhatsappEntity entity) {
        if (entity == null) return null;

        return SesionWhatsapp.builder()
                .id(entity.getId())
                .numeroWhatsapp(entity.getNumeroWhatsapp())
                .usuarioId(entity.getUsuarioId())
                .token(entity.getToken())
                .refreshToken(entity.getRefreshToken())
                .activa(entity.isActiva())
                .fechaExpiracion(entity.getFechaExpiracion())
                .ultimaActividad(entity.getUltimaActividad())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public SesionWhatsappEntity toEntity(SesionWhatsapp domain) {
        if (domain == null) return null;

        return SesionWhatsappEntity.builder()
                .id(domain.getId())
                .numeroWhatsapp(domain.getNumeroWhatsapp())
                .usuarioId(domain.getUsuarioId())
                .token(domain.getToken())
                .refreshToken(domain.getRefreshToken())
                .activa(domain.isActiva())
                .fechaExpiracion(domain.getFechaExpiracion())
                .ultimaActividad(domain.getUltimaActividad())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
