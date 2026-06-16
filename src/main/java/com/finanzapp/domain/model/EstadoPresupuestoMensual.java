package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoPresupuestoMensual {
    private UUID presupuestoMensualId;
    private Integer anio;
    private Integer mes;
    private BigDecimal baseCalculada;
    private BigDecimal totalAsignado;
    private BigDecimal totalGastado;
    private BigDecimal totalRecurrente;
    private BigDecimal totalComprometido;
    private List<EstadoBolsilloMensual> bolsillos;
}
