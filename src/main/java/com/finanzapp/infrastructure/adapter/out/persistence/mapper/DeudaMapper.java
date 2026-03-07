package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Deuda;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.DeudaEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class DeudaMapper {

    public Deuda toDomain(DeudaEntity entity) {
        if (entity == null) return null;

        String categoriaDescripcion = null;
        String categoriaColor = null;

        if (entity.getCategoriaPersonalizada() != null) {
            categoriaDescripcion = entity.getCategoriaPersonalizada().getNombre();
            categoriaColor = entity.getCategoriaPersonalizada().getColor();
        } else if (entity.getCategoria() != null) {
            try {
                categoriaDescripcion = CategoriaGasto.valueOf(entity.getCategoria()).getDescripcion();
            } catch (IllegalArgumentException e) {
                categoriaDescripcion = entity.getCategoria();
            }
        }

        return Deuda.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuario().getId())
                .tipo(entity.getTipo())
                .descripcion(entity.getDescripcion())
                .entidad(entity.getEntidad())
                .categoria(entity.getCategoria())
                .categoriaPersonalizadaId(entity.getCategoriaPersonalizadaId())
                .categoriaDescripcion(categoriaDescripcion)
                .categoriaColor(categoriaColor)
                .montoTotal(entity.getMontoTotal())
                .montoAbonado(entity.getMontoAbonado())
                .montoRestante(entity.getMontoRestante())
                .estado(entity.getEstado())
                .fechaInicio(entity.getFechaInicio())
                .fechaLimite(entity.getFechaLimite())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public DeudaEntity toEntity(Deuda domain, UsuarioEntity usuario) {
        if (domain == null) return null;

        return DeudaEntity.builder()
                .id(domain.getId())
                .usuario(usuario)
                .tipo(domain.getTipo())
                .descripcion(domain.getDescripcion())
                .entidad(domain.getEntidad())
                .categoria(domain.getCategoria())
                .categoriaPersonalizadaId(domain.getCategoriaPersonalizadaId())
                .montoTotal(domain.getMontoTotal())
                .montoAbonado(domain.getMontoAbonado())
                .montoRestante(domain.getMontoRestante())
                .estado(domain.getEstado())
                .fechaInicio(domain.getFechaInicio())
                .fechaLimite(domain.getFechaLimite())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
