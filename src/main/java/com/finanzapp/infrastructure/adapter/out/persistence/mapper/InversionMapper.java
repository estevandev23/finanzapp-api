package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.Inversion;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.InversionEntity;
import org.springframework.stereotype.Component;

@Component
public class InversionMapper {

    public Inversion toDomain(InversionEntity entity) {
        if (entity == null) return null;

        return Inversion.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .gastoId(entity.getGastoId())
                .ingresoId(entity.getIngresoId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .monto(entity.getMonto())
                .retornoEsperado(entity.getRetornoEsperado())
                .retornoReal(entity.getRetornoReal())
                .estado(entity.getEstado())
                .fechaInversion(entity.getFechaInversion())
                .fechaRetorno(entity.getFechaRetorno())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public InversionEntity toEntity(Inversion domain) {
        if (domain == null) return null;

        return InversionEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .gastoId(domain.getGastoId())
                .ingresoId(domain.getIngresoId())
                .nombre(domain.getNombre())
                .descripcion(domain.getDescripcion())
                .monto(domain.getMonto())
                .retornoEsperado(domain.getRetornoEsperado())
                .retornoReal(domain.getRetornoReal())
                .estado(domain.getEstado())
                .fechaInversion(domain.getFechaInversion())
                .fechaRetorno(domain.getFechaRetorno())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
