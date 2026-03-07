package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.model.GastoMetodoPago;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.GastoEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.GastoMetodoPagoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        List<GastoMetodoPago> metodos = entity.getMetodosPago() != null
                ? entity.getMetodosPago().stream()
                    .map(m -> GastoMetodoPago.builder()
                            .id(m.getId())
                            .gastoId(m.getGastoId())
                            .metodo(m.getMetodo())
                            .monto(m.getMonto())
                            .build())
                    .toList()
                : Collections.emptyList();

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
                .metodosPago(metodos)
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public GastoEntity toEntity(Gasto domain) {
        if (domain == null) return null;

        GastoEntity entity = GastoEntity.builder()
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

        List<GastoMetodoPagoEntity> metodos = domain.getMetodosPago() != null
                ? new ArrayList<>(domain.getMetodosPago().stream()
                    .map(m -> GastoMetodoPagoEntity.builder()
                            .id(m.getId())
                            .gasto(entity)
                            .metodo(m.getMetodo())
                            .monto(m.getMonto())
                            .build())
                    .toList())
                : new ArrayList<>();

        entity.setMetodosPago(metodos);
        return entity;
    }
}
