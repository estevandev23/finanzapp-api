package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.Usuario;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) return null;

        return Usuario.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .telefono(entity.getTelefono())
                .oauthProvider(entity.getOauthProvider())
                .oauthProviderId(entity.getOauthProviderId())
                .dosFactoresActivado(entity.isDosFactoresActivado())
                .telefonoVerificado(entity.isTelefonoVerificado())
                .activo(entity.isActivo())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) return null;

        return UsuarioEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .telefono(domain.getTelefono())
                .oauthProvider(domain.getOauthProvider())
                .oauthProviderId(domain.getOauthProviderId())
                .dosFactoresActivado(domain.isDosFactoresActivado())
                .telefonoVerificado(domain.isTelefonoVerificado())
                .activo(domain.isActivo())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
