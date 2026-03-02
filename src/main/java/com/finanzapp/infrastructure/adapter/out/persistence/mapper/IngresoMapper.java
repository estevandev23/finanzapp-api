package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.Ingreso;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.IngresoEntity;
import org.springframework.stereotype.Component;

@Component
public class IngresoMapper {

    public Ingreso toDomain(IngresoEntity entity) {
        if (entity == null) return null;

        String categoriaNombre = entity.getCategoriaPersonalizada() != null
                ? entity.getCategoriaPersonalizada().getNombre()
                : (entity.getCategoria() != null ? entity.getCategoria().getDescripcion() : null);

        String categoriaColor = entity.getCategoriaPersonalizada() != null
                ? entity.getCategoriaPersonalizada().getColor()
                : null;

        return Ingreso.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .monto(entity.getMonto())
                .categoria(entity.getCategoria())
                .categoriaNombre(categoriaNombre)
                .categoriaColor(categoriaColor)
                .descripcion(entity.getDescripcion())
                .fecha(entity.getFecha())
                .montoAhorro(entity.getMontoAhorro())
                .categoriaPersonalizadaId(entity.getCategoriaPersonalizadaId())
                .prestamoId(entity.getPrestamoId())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public IngresoEntity toEntity(Ingreso domain) {
        if (domain == null) return null;

        return IngresoEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .monto(domain.getMonto())
                .categoria(domain.getCategoria())
                .descripcion(domain.getDescripcion())
                .fecha(domain.getFecha())
                .montoAhorro(domain.getMontoAhorro())
                .categoriaPersonalizadaId(domain.getCategoriaPersonalizadaId())
                .prestamoId(domain.getPrestamoId())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
