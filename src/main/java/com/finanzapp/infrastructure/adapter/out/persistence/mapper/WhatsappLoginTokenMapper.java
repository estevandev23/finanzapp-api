package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.WhatsappLoginToken;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.WhatsappLoginTokenEntity;
import org.springframework.stereotype.Component;

@Component
public class WhatsappLoginTokenMapper {

    public WhatsappLoginToken toDomain(WhatsappLoginTokenEntity entity) {
        if (entity == null) return null;

        return WhatsappLoginToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .numeroWhatsapp(entity.getNumeroWhatsapp())
                .usado(entity.isUsado())
                .fechaExpiracion(entity.getFechaExpiracion())
                .fechaCreacion(entity.getFechaCreacion())
                .build();
    }

    public WhatsappLoginTokenEntity toEntity(WhatsappLoginToken domain) {
        if (domain == null) return null;

        return WhatsappLoginTokenEntity.builder()
                .id(domain.getId())
                .token(domain.getToken())
                .numeroWhatsapp(domain.getNumeroWhatsapp())
                .usado(domain.isUsado())
                .fechaExpiracion(domain.getFechaExpiracion())
                .fechaCreacion(domain.getFechaCreacion())
                .build();
    }
}
