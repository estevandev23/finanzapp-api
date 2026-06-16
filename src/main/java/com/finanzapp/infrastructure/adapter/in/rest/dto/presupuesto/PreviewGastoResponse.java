package com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto;

import com.finanzapp.domain.model.NivelAlertaPresupuesto;
import com.finanzapp.domain.model.PreviewGastoBolsillo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewGastoResponse {
    private UUID bolsilloOrigenId;
    private String nombre;
    private String color;
    private BigDecimal montoLimite;
    private BigDecimal montoNuevoGasto;
    private BigDecimal montoGastadoActual;
    private BigDecimal porcentajeUsoActual;
    private BigDecimal montoProyectado;
    private BigDecimal porcentajeUsoProyectado;
    private BigDecimal montoRestanteProyectado;
    private NivelAlertaPresupuesto nivelActual;
    private NivelAlertaPresupuesto nivelProyectado;
    private boolean excedeProyectado;

    public static PreviewGastoResponse fromDomain(PreviewGastoBolsillo p) {
        return PreviewGastoResponse.builder()
                .bolsilloOrigenId(p.getBolsilloOrigenId())
                .nombre(p.getNombre())
                .color(p.getColor())
                .montoLimite(p.getMontoLimite())
                .montoNuevoGasto(p.getMontoNuevoGasto())
                .montoGastadoActual(p.getMontoGastadoActual())
                .porcentajeUsoActual(p.getPorcentajeUsoActual())
                .montoProyectado(p.getMontoProyectado())
                .porcentajeUsoProyectado(p.getPorcentajeUsoProyectado())
                .montoRestanteProyectado(p.getMontoRestanteProyectado())
                .nivelActual(p.getNivelActual())
                .nivelProyectado(p.getNivelProyectado())
                .excedeProyectado(p.isExcedeProyectado())
                .build();
    }
}
