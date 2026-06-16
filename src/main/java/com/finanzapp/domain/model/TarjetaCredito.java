package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaCredito {
    private UUID id;
    private UUID usuarioId;
    private String nombre;
    private String banco;
    private String ultimosCuatro;
    private BigDecimal cupoTotal;
    private BigDecimal cupoUsado;
    private int diaCorte;
    private int diaPago;
    private String color;
    private EstadoTarjeta estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public BigDecimal getCupoDisponible() {
        if (cupoTotal == null) return BigDecimal.ZERO;
        BigDecimal usado = cupoUsado != null ? cupoUsado : BigDecimal.ZERO;
        return cupoTotal.subtract(usado).max(BigDecimal.ZERO);
    }

    public BigDecimal getPorcentajeUtilizacion() {
        if (cupoTotal == null || cupoTotal.signum() == 0) return BigDecimal.ZERO;
        BigDecimal usado = cupoUsado != null ? cupoUsado : BigDecimal.ZERO;
        return usado.multiply(BigDecimal.valueOf(100))
                .divide(cupoTotal, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el primer día del mes en que un gasto con la fecha indicada quedará
     * facturado y debe pagarse. Regla: si la fecha es igual o anterior al día de corte
     * del mismo mes, el extracto se cierra ese mes y se paga el siguiente. Si es
     * posterior al día de corte, queda en el extracto del mes siguiente (pago dos meses
     * después).
     */
    public LocalDate calcularMesFacturacion(LocalDate fechaCompra) {
        if (fechaCompra == null) {
            throw new IllegalArgumentException("fechaCompra es requerida");
        }
        int diaCorteAjustado = Math.min(diaCorte, fechaCompra.lengthOfMonth());
        boolean despuesDelCorte = fechaCompra.getDayOfMonth() > diaCorteAjustado;
        LocalDate base = despuesDelCorte ? fechaCompra.plusMonths(2) : fechaCompra.plusMonths(1);
        return base.withDayOfMonth(1);
    }

    public boolean puedeUsarse(BigDecimal monto) {
        if (estado != EstadoTarjeta.ACTIVA) return false;
        if (monto == null || monto.signum() <= 0) return false;
        return getCupoDisponible().compareTo(monto) >= 0;
    }
}
