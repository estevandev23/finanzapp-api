package com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto;

import com.finanzapp.domain.model.EstadoBolsilloMensual;
import com.finanzapp.domain.model.MovimientoBolsillo;
import com.finanzapp.domain.model.NivelAlertaPresupuesto;
import com.finanzapp.domain.model.TipoBolsillo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoBolsilloResponse {
    private UUID bolsilloMensualId;
    private UUID bolsilloOrigenId;
    private String nombre;
    private TipoBolsillo tipo;
    private String tipoDescripcion;
    private BigDecimal porcentaje;
    private BigDecimal montoLimite;
    private BigDecimal montoGastado;
    private BigDecimal montoRecurrente;
    private BigDecimal montoComprometido;
    private BigDecimal porcentajeUso;
    private NivelAlertaPresupuesto nivel;
    private String color;
    private Integer orden;
    private List<MovimientoBolsilloResponse> movimientos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovimientoBolsilloResponse {
        private String tipo;
        private UUID id;
        private String descripcion;
        private BigDecimal monto;
        private LocalDate fecha;
        private String categoriaNombre;

        public static MovimientoBolsilloResponse fromDomain(MovimientoBolsillo m) {
            return MovimientoBolsilloResponse.builder()
                    .tipo(m.getTipo() != null ? m.getTipo().name() : null)
                    .id(m.getId())
                    .descripcion(m.getDescripcion())
                    .monto(m.getMonto())
                    .fecha(m.getFecha())
                    .categoriaNombre(m.getCategoriaNombre())
                    .build();
        }
    }

    public static EstadoBolsilloResponse fromDomain(EstadoBolsilloMensual e) {
        List<MovimientoBolsilloResponse> movimientos = e.getMovimientos() != null
                ? e.getMovimientos().stream().map(MovimientoBolsilloResponse::fromDomain).toList()
                : Collections.emptyList();
        return EstadoBolsilloResponse.builder()
                .bolsilloMensualId(e.getBolsillo().getId())
                .bolsilloOrigenId(e.getBolsillo().getBolsilloOrigenId())
                .nombre(e.getBolsillo().getNombre())
                .tipo(e.getBolsillo().getTipo())
                .tipoDescripcion(e.getBolsillo().getTipo() != null
                        ? e.getBolsillo().getTipo().getDescripcion() : null)
                .porcentaje(e.getBolsillo().getPorcentaje())
                .montoLimite(e.getBolsillo().getMontoLimite())
                .montoGastado(e.getMontoGastado())
                .montoRecurrente(e.getMontoRecurrente())
                .montoComprometido(e.getMontoComprometido())
                .porcentajeUso(e.getPorcentajeUso())
                .nivel(e.getNivel())
                .color(e.getBolsillo().getColor())
                .orden(e.getBolsillo().getOrden())
                .movimientos(movimientos)
                .build();
    }
}
