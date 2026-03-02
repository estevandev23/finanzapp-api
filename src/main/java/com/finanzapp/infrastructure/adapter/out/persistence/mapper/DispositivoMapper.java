package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.DispositivoEntity;
import org.springframework.stereotype.Component;

@Component
public class DispositivoMapper {

    public Dispositivo toDomain(DispositivoEntity entity) {
        if (entity == null) return null;

        return Dispositivo.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .numeroWhatsapp(entity.getNumeroWhatsapp())
                .nombreDispositivo(entity.getNombreDispositivo())
                .tokenDispositivo(entity.getTokenDispositivo())
                .activo(entity.isActivo())
                .verificado(entity.isVerificado())
                .codigoVerificacion(entity.getCodigoVerificacion())
                .fechaExpiracionCodigo(entity.getFechaExpiracionCodigo())
                .ultimaConexion(entity.getUltimaConexion())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public DispositivoEntity toEntity(Dispositivo domain) {
        if (domain == null) return null;

        return DispositivoEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .numeroWhatsapp(domain.getNumeroWhatsapp())
                .nombreDispositivo(domain.getNombreDispositivo())
                .tokenDispositivo(domain.getTokenDispositivo())
                .activo(domain.isActivo())
                .verificado(domain.isVerificado())
                .codigoVerificacion(domain.getCodigoVerificacion())
                .fechaExpiracionCodigo(domain.getFechaExpiracionCodigo())
                .ultimaConexion(domain.getUltimaConexion())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
