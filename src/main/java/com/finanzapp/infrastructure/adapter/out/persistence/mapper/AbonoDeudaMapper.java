package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.AbonoDeuda;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.AbonoDeudaEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.DeudaEntity;
import org.springframework.stereotype.Component;

@Component
public class AbonoDeudaMapper {

    public AbonoDeuda toDomain(AbonoDeudaEntity entity) {
        if (entity == null) return null;

        return AbonoDeuda.builder()
                .id(entity.getId())
                .deudaId(entity.getDeuda().getId())
                .gastoId(entity.getGastoId())
                .ingresoId(entity.getIngresoId())
                .monto(entity.getMonto())
                .descripcion(entity.getDescripcion())
                .fechaAbono(entity.getFechaAbono())
                .fechaCreacion(entity.getFechaCreacion())
                .build();
    }

    public AbonoDeudaEntity toEntity(AbonoDeuda domain, DeudaEntity deuda) {
        if (domain == null) return null;

        return AbonoDeudaEntity.builder()
                .id(domain.getId())
                .deuda(deuda)
                .gastoId(domain.getGastoId())
                .ingresoId(domain.getIngresoId())
                .monto(domain.getMonto())
                .descripcion(domain.getDescripcion())
                .fechaAbono(domain.getFechaAbono())
                .fechaCreacion(domain.getFechaCreacion())
                .build();
    }
}
