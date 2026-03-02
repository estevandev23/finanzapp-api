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
public class Deuda {
    private UUID id;
    private UUID usuarioId;
    private TipoDeuda tipo;
    private String descripcion;
    private String entidad;
    private BigDecimal montoTotal;
    private BigDecimal montoAbonado;
    private BigDecimal montoRestante;
    private EstadoDeuda estado;
    private LocalDate fechaInicio;
    private LocalDate fechaLimite;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public BigDecimal getPorcentajeAvance() {
        if (montoTotal == null || montoTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (montoAbonado == null) {
            return BigDecimal.ZERO;
        }
        return montoAbonado.multiply(BigDecimal.valueOf(100))
                .divide(montoTotal, 2, RoundingMode.HALF_UP);
    }

    public boolean isCompletada() {
        return montoAbonado != null && montoAbonado.compareTo(montoTotal) >= 0;
    }

    public void recalcularRestante() {
        if (montoTotal != null && montoAbonado != null) {
            this.montoRestante = montoTotal.subtract(montoAbonado).max(BigDecimal.ZERO);
        }
    }
}
