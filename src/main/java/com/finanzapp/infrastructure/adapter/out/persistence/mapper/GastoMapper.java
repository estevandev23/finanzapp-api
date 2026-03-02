package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.Gasto;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.GastoEntity;
import org.springframework.stereotype.Component;

@Component
public class GastoMapper {

    public Gasto toDomain(GastoEntity entity) {
        if (entity == null) return null;

        String categoriaNombre = entity.getCategoriaPersonalizada() != null
                ? entity.getCategoriaPersonalizada().getNombre()
                : (entity.getCategoria() != null ? entity.getCategoria().getDescripcion() : null);

        String categoriaColor = entity.getCategoriaPersonalizada() != null
                ? entity.getCategoriaPersonalizada().getColor()
                : null;

        return Gasto.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .monto(entity.getMonto())
                .categoria(entity.getCategoria())
                .categoriaNombre(categoriaNombre)
                .categoriaColor(categoriaColor)
                .categoriaPersonalizadaId(entity.getCategoriaPersonalizadaId())
                .deudaId(entity.getDeudaId())
                .descripcion(entity.getDescripcion())
                .fecha(entity.getFecha())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public GastoEntity toEntity(Gasto domain) {
        if (domain == null) return null;

        return GastoEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .monto(domain.getMonto())
                .categoria(domain.getCategoria())
                .categoriaPersonalizadaId(domain.getCategoriaPersonalizadaId())
                .deudaId(domain.getDeudaId())
                .descripcion(domain.getDescripcion())
                .fecha(domain.getFecha())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
