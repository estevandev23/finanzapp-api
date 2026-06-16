package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.TarjetaCredito;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.TarjetaCreditoEntity;
import org.springframework.stereotype.Component;

@Component
public class TarjetaCreditoMapper {

    public TarjetaCredito toDomain(TarjetaCreditoEntity entity) {
        if (entity == null) return null;
        return TarjetaCredito.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .nombre(entity.getNombre())
                .banco(entity.getBanco())
                .ultimosCuatro(entity.getUltimosCuatro())
                .cupoTotal(entity.getCupoTotal())
                .cupoUsado(entity.getCupoUsado())
                .diaCorte(entity.getDiaCorte())
                .diaPago(entity.getDiaPago())
                .color(entity.getColor())
                .estado(entity.getEstado())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public TarjetaCreditoEntity toEntity(TarjetaCredito domain) {
        if (domain == null) return null;
        return TarjetaCreditoEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .nombre(domain.getNombre())
                .banco(domain.getBanco())
                .ultimosCuatro(domain.getUltimosCuatro())
                .cupoTotal(domain.getCupoTotal())
                .cupoUsado(domain.getCupoUsado())
                .diaCorte(domain.getDiaCorte())
                .diaPago(domain.getDiaPago())
                .color(domain.getColor())
                .estado(domain.getEstado())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
