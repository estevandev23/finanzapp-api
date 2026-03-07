package com.finanzapp.infrastructure.adapter.in.rest.dto.balance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalancePorMetodoResponse {

    private List<MetodoBalance> metodos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetodoBalance {
        private String metodo;
        private BigDecimal totalIngresos;
        private BigDecimal totalGastos;
        private BigDecimal balance;
    }
}
