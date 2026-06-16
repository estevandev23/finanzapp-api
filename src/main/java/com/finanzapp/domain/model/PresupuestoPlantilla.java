package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoPlantilla {
    private UUID id;
    private UUID usuarioId;
    private TipoBasePresupuesto tipoBase;
    private BigDecimal montoFijo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @Builder.Default
    private List<Bolsillo> bolsillos = new ArrayList<>();

    /**
     * Suma de los porcentajes de todos los bolsillos.
     * Debe ser <= 100. Lo restante se considera "libre" (no asignado).
     */
    public BigDecimal sumaPorcentajes() {
        if (bolsillos == null || bolsillos.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return bolsillos.stream()
                .map(Bolsillo::getPorcentaje)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
