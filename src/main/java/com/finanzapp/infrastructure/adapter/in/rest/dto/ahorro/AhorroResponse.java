package com.finanzapp.infrastructure.adapter.in.rest.dto.ahorro;

import com.finanzapp.domain.model.Ahorro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AhorroResponse {
    private UUID id;
    private BigDecimal monto;
    private String descripcion;
    private LocalDate fecha;
    private UUID metaId;
    private UUID ingresoId;
    private LocalDateTime fechaCreacion;

    public static AhorroResponse fromDomain(Ahorro ahorro) {
        return AhorroResponse.builder()
                .id(ahorro.getId())
                .monto(ahorro.getMonto())
                .descripcion(ahorro.getDescripcion())
                .fecha(ahorro.getFecha())
                .metaId(ahorro.getMetaId())
                .ingresoId(ahorro.getIngresoId())
                .fechaCreacion(ahorro.getFechaCreacion())
                .build();
    }
}
