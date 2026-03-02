package com.finanzapp.infrastructure.adapter.in.rest.dto.deuda;

import com.finanzapp.domain.model.AbonoDeuda;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbonoResponse {
    private UUID id;
    private UUID deudaId;
    private UUID gastoId;
    private UUID ingresoId;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fechaAbono;

    public static AbonoResponse fromDomain(AbonoDeuda abono) {
        return AbonoResponse.builder()
                .id(abono.getId())
                .deudaId(abono.getDeudaId())
                .gastoId(abono.getGastoId())
                .ingresoId(abono.getIngresoId())
                .monto(abono.getMonto())
                .descripcion(abono.getDescripcion())
                .fechaAbono(abono.getFechaAbono())
                .build();
    }
}
