package com.finanzapp.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Balance {
    private UUID usuarioId;
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal totalAhorros;
    private BigDecimal totalAhorrosDesdeIngresos;
    private BigDecimal totalDeudas;
    private BigDecimal totalPrestamos;
    private BigDecimal dineroDisponible;

    public static Balance calcular(BigDecimal ingresos, BigDecimal gastos, BigDecimal ahorrosTotales,
                                    BigDecimal ahorrosDesdeIngresos) {
        return calcular(ingresos, gastos, ahorrosTotales, ahorrosDesdeIngresos, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static Balance calcular(BigDecimal ingresos, BigDecimal gastos, BigDecimal ahorrosTotales,
                                    BigDecimal ahorrosDesdeIngresos, BigDecimal deudas, BigDecimal prestamos) {
        BigDecimal disponible = ingresos.subtract(gastos).subtract(ahorrosDesdeIngresos).subtract(deudas);
        return Balance.builder()
                .totalIngresos(ingresos)
                .totalGastos(gastos)
                .totalAhorros(ahorrosTotales)
                .totalAhorrosDesdeIngresos(ahorrosDesdeIngresos)
                .totalDeudas(deudas)
                .totalPrestamos(prestamos)
                .dineroDisponible(disponible)
                .build();
    }
}
