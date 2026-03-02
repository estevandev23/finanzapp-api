package com.finanzapp.infrastructure.adapter.in.rest.dto.inversion;

import com.finanzapp.domain.model.EstadoInversion;
import com.finanzapp.domain.model.Inversion;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InversionResponse {
    private UUID id;
    private UUID usuarioId;
    private UUID gastoId;
    private UUID ingresoId;
    private String nombre;
    private String descripcion;
    private BigDecimal monto;
    private BigDecimal retornoEsperado;
    private BigDecimal retornoReal;
    private BigDecimal ganancia;
    private EstadoInversion estado;
    private LocalDate fechaInversion;
    private LocalDate fechaRetorno;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public static InversionResponse fromDomain(Inversion inversion) {
        return InversionResponse.builder()
                .id(inversion.getId())
                .usuarioId(inversion.getUsuarioId())
                .gastoId(inversion.getGastoId())
                .ingresoId(inversion.getIngresoId())
                .nombre(inversion.getNombre())
                .descripcion(inversion.getDescripcion())
                .monto(inversion.getMonto())
                .retornoEsperado(inversion.getRetornoEsperado())
                .retornoReal(inversion.getRetornoReal())
                .ganancia(inversion.calcularGanancia())
                .estado(inversion.getEstado())
                .fechaInversion(inversion.getFechaInversion())
                .fechaRetorno(inversion.getFechaRetorno())
                .fechaCreacion(inversion.getFechaCreacion())
                .fechaActualizacion(inversion.getFechaActualizacion())
                .build();
    }
}
