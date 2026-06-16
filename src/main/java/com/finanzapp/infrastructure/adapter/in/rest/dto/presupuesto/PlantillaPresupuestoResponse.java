package com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto;

import com.finanzapp.domain.model.PresupuestoPlantilla;
import com.finanzapp.domain.model.TipoBasePresupuesto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaPresupuestoResponse {
    private UUID id;
    private UUID usuarioId;
    private TipoBasePresupuesto tipoBase;
    private String tipoBaseDescripcion;
    private BigDecimal montoFijo;
    private BigDecimal sumaPorcentajes;
    private List<BolsilloResponse> bolsillos;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public static PlantillaPresupuestoResponse fromDomain(PresupuestoPlantilla p) {
        List<BolsilloResponse> bolsillos = p.getBolsillos() != null
                ? p.getBolsillos().stream().map(BolsilloResponse::fromDomain).toList()
                : Collections.emptyList();

        return PlantillaPresupuestoResponse.builder()
                .id(p.getId())
                .usuarioId(p.getUsuarioId())
                .tipoBase(p.getTipoBase())
                .tipoBaseDescripcion(p.getTipoBase() != null ? p.getTipoBase().getDescripcion() : null)
                .montoFijo(p.getMontoFijo())
                .sumaPorcentajes(p.sumaPorcentajes())
                .bolsillos(bolsillos)
                .fechaCreacion(p.getFechaCreacion())
                .fechaActualizacion(p.getFechaActualizacion())
                .build();
    }
}
