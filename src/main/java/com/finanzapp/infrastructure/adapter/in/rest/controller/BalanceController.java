package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.Balance;
import com.finanzapp.domain.port.in.BalanceUseCase;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.balance.BalancePorMetodoResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.balance.BalanceResponse;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/balance")
@RequiredArgsConstructor
@Tag(name = "Balance", description = "Consulta de balance financiero del usuario")
public class BalanceController {

    private final BalanceUseCase balanceUseCase;

    @GetMapping
    @Operation(summary = "Obtener balance general", description = "Obtiene el balance financiero general del usuario")
    public ResponseEntity<ApiResponse<BalanceResponse>> obtenerBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Balance balance = balanceUseCase.obtenerBalanceGeneral(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(BalanceResponse.fromDomain(balance)));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Obtener balance por periodo", description = "Obtiene el balance financiero en un rango de fechas")
    public ResponseEntity<ApiResponse<BalanceResponse>> obtenerBalancePorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        Balance balance = balanceUseCase.obtenerBalancePorPeriodo(userDetails.getId(), fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success(BalanceResponse.fromDomain(balance)));
    }

    @GetMapping("/metodos")
    @Operation(summary = "Obtener balance por método de pago", description = "Obtiene el balance desglosado por cada método de pago")
    public ResponseEntity<ApiResponse<BalancePorMetodoResponse>> obtenerBalancePorMetodo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, BigDecimal[]> balancePorMetodo = balanceUseCase.obtenerBalancePorMetodoPago(userDetails.getId());

        List<BalancePorMetodoResponse.MetodoBalance> metodos = new ArrayList<>();
        balancePorMetodo.forEach((metodo, montos) -> metodos.add(
                BalancePorMetodoResponse.MetodoBalance.builder()
                        .metodo(metodo)
                        .totalIngresos(montos[0])
                        .totalGastos(montos[1])
                        .balance(montos[0].subtract(montos[1]))
                        .build()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                BalancePorMetodoResponse.builder().metodos(metodos).build()));
    }
}
