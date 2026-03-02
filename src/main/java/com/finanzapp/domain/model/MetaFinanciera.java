package com.finanzapp.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaFinanciera {
    private UUID id;
    private UUID usuarioId;
    private String nombre;
    private String descripcion;
    private BigDecimal montoObjetivo;
    private BigDecimal montoActual;
    private LocalDate fechaLimite;
    private EstadoMeta estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public BigDecimal getPorcentajeAvance() {
        if (montoObjetivo == null || montoObjetivo.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (montoActual == null) {
            return BigDecimal.ZERO;
        }
        return montoActual.multiply(BigDecimal.valueOf(100))
                .divide(montoObjetivo, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMontoRestante() {
        if (montoObjetivo == null) {
            return BigDecimal.ZERO;
        }
        if (montoActual == null) {
            return montoObjetivo;
        }
        BigDecimal restante = montoObjetivo.subtract(montoActual);
        return restante.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : restante;
    }

    public boolean isCompletada() {
        return montoActual != null && montoActual.compareTo(montoObjetivo) >= 0;
    }
}
