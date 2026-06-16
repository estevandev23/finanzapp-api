package com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto;

import com.finanzapp.domain.model.EstadoPresupuestoMensual;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoPresupuestoResponse {
    private UUID presupuestoMensualId;
    private Integer anio;
    private Integer mes;
    private BigDecimal baseCalculada;
    private BigDecimal totalAsignado;
    private BigDecimal totalGastado;
    private BigDecimal totalRecurrente;
    private BigDecimal totalComprometido;
    private BigDecimal totalDisponible;
    private List<EstadoBolsilloResponse> bolsillos;

    public static EstadoPresupuestoResponse fromDomain(EstadoPresupuestoMensual e) {
        BigDecimal asignado = e.getTotalAsignado() != null ? e.getTotalAsignado() : BigDecimal.ZERO;
        BigDecimal gastado = e.getTotalGastado() != null ? e.getTotalGastado() : BigDecimal.ZERO;
        BigDecimal recurrente = e.getTotalRecurrente() != null ? e.getTotalRecurrente() : BigDecimal.ZERO;
        BigDecimal comprometido = e.getTotalComprometido() != null
                ? e.getTotalComprometido() : gastado.add(recurrente);
        List<EstadoBolsilloResponse> bolsillos = e.getBolsillos() != null
                ? e.getBolsillos().stream().map(EstadoBolsilloResponse::fromDomain).toList()
                : Collections.emptyList();
        return EstadoPresupuestoResponse.builder()
                .presupuestoMensualId(e.getPresupuestoMensualId())
                .anio(e.getAnio())
                .mes(e.getMes())
                .baseCalculada(e.getBaseCalculada())
                .totalAsignado(asignado)
                .totalGastado(gastado)
                .totalRecurrente(recurrente)
                .totalComprometido(comprometido)
                .totalDisponible(asignado.subtract(comprometido))
                .bolsillos(bolsillos)
                .build();
    }
}
