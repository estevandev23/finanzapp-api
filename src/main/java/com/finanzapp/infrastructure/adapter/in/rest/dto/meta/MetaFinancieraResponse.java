package com.finanzapp.infrastructure.adapter.in.rest.dto.meta;

import com.finanzapp.domain.model.EstadoMeta;
import com.finanzapp.domain.model.MetaFinanciera;
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
public class MetaFinancieraResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private BigDecimal montoObjetivo;
    private BigDecimal montoActual;
    private BigDecimal montoRestante;
    private BigDecimal porcentajeAvance;
    private LocalDate fechaLimite;
    private EstadoMeta estado;
    private String estadoDescripcion;
    private boolean completada;
    private LocalDateTime fechaCreacion;

    public static MetaFinancieraResponse fromDomain(MetaFinanciera meta) {
        return MetaFinancieraResponse.builder()
                .id(meta.getId())
                .nombre(meta.getNombre())
                .descripcion(meta.getDescripcion())
                .montoObjetivo(meta.getMontoObjetivo())
                .montoActual(meta.getMontoActual())
                .montoRestante(meta.getMontoRestante())
                .porcentajeAvance(meta.getPorcentajeAvance())
                .fechaLimite(meta.getFechaLimite())
                .estado(meta.getEstado())
                .estadoDescripcion(meta.getEstado().getDescripcion())
                .completada(meta.isCompletada())
                .fechaCreacion(meta.getFechaCreacion())
                .build();
    }
}
