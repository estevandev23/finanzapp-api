package com.finanzapp.infrastructure.adapter.in.rest.dto.balance;

import com.finanzapp.domain.model.Balance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal totalAhorros;
    private BigDecimal totalAhorrosDesdeIngresos;
    private BigDecimal totalDeudas;
    private BigDecimal totalPrestamos;
    private BigDecimal dineroDisponible;

    public static BalanceResponse fromDomain(Balance balance) {
        return BalanceResponse.builder()
                .totalIngresos(balance.getTotalIngresos())
                .totalGastos(balance.getTotalGastos())
                .totalAhorros(balance.getTotalAhorros())
                .totalAhorrosDesdeIngresos(balance.getTotalAhorrosDesdeIngresos())
                .totalDeudas(balance.getTotalDeudas())
                .totalPrestamos(balance.getTotalPrestamos())
                .dineroDisponible(balance.getDineroDisponible())
                .build();
    }
}
