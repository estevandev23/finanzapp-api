package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.RecurrenciaEntity;
import org.springframework.stereotype.Component;

@Component
public class RecurrenciaMapper {

    public Recurrencia toDomain(RecurrenciaEntity entity) {
        if (entity == null) return null;

        String categoriaNombre;
        String categoriaColor = null;
        if (entity.getCategoriaPersonalizada() != null) {
            categoriaNombre = entity.getCategoriaPersonalizada().getNombre();
            categoriaColor = entity.getCategoriaPersonalizada().getColor();
        } else if (entity.getCategoriaIngreso() != null) {
            categoriaNombre = entity.getCategoriaIngreso().getDescripcion();
        } else if (entity.getCategoriaGasto() != null) {
            categoriaNombre = entity.getCategoriaGasto().getDescripcion();
        } else {
            categoriaNombre = null;
        }

        return Recurrencia.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .tipo(entity.getTipo())
                .frecuencia(entity.getFrecuencia())
                .descripcion(entity.getDescripcion())
                .monto(entity.getMonto())
                .categoriaIngreso(entity.getCategoriaIngreso())
                .categoriaGasto(entity.getCategoriaGasto())
                .categoriaPersonalizadaId(entity.getCategoriaPersonalizadaId())
                .categoriaNombre(categoriaNombre)
                .categoriaColor(categoriaColor)
                .metodoPago(entity.getMetodoPago())
                .tarjetaId(entity.getTarjetaId())
                .tarjetaNombre(entity.getTarjeta() != null ? entity.getTarjeta().getNombre() : null)
                .bolsilloId(entity.getBolsilloId())
                .bolsilloNombre(entity.getBolsillo() != null ? entity.getBolsillo().getNombre() : null)
                .diaVencimiento(entity.getDiaVencimiento())
                .mesReferencia(entity.getMesReferencia())
                .proximaFecha(entity.getProximaFecha())
                .ultimaConfirmacionFecha(entity.getUltimaConfirmacionFecha())
                .activa(entity.isActiva())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public RecurrenciaEntity toEntity(Recurrencia domain) {
        if (domain == null) return null;

        return RecurrenciaEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .tipo(domain.getTipo())
                .frecuencia(domain.getFrecuencia())
                .descripcion(domain.getDescripcion())
                .monto(domain.getMonto())
                .categoriaIngreso(domain.getCategoriaIngreso())
                .categoriaGasto(domain.getCategoriaGasto())
                .categoriaPersonalizadaId(domain.getCategoriaPersonalizadaId())
                .metodoPago(domain.getMetodoPago())
                .tarjetaId(domain.getTarjetaId())
                .bolsilloId(domain.getBolsilloId())
                .diaVencimiento(domain.getDiaVencimiento())
                .mesReferencia(domain.getMesReferencia())
                .proximaFecha(domain.getProximaFecha())
                .ultimaConfirmacionFecha(domain.getUltimaConfirmacionFecha())
                .activa(domain.isActiva())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
