package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.TarjetaCreditoService;
import com.finanzapp.domain.model.TarjetaCredito;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.tarjeta.TarjetaCreditoRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.tarjeta.TarjetaCreditoResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.tarjeta.TarjetaCreditoUpdateRequest;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tarjetas")
@RequiredArgsConstructor
@Tag(name = "Tarjetas de Crédito",
        description = "Gestión de tarjetas con día de corte y pago. Los gastos pagados con tarjeta se contabilizan en el mes de facturación, no en el mes de la compra.")
public class TarjetaCreditoController {

    private final TarjetaCreditoService tarjetaService;

    @PostMapping
    @Operation(summary = "Crear tarjeta de crédito")
    public ResponseEntity<ApiResponse<TarjetaCreditoResponse>> crear(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TarjetaCreditoRequest request) {

        TarjetaCredito tarjeta = TarjetaCredito.builder()
                .usuarioId(userDetails.getId())
                .nombre(request.getNombre())
                .banco(request.getBanco())
                .ultimosCuatro(request.getUltimosCuatro())
                .cupoTotal(request.getCupoTotal())
                .diaCorte(request.getDiaCorte())
                .diaPago(request.getDiaPago())
                .color(request.getColor())
                .estado(request.getEstado())
                .build();

        TarjetaCredito creada = tarjetaService.crear(tarjeta);
        return ResponseEntity.ok(ApiResponse.success(
                TarjetaCreditoResponse.fromDomain(creada),
                "Tarjeta creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar tarjetas del usuario")
    public ResponseEntity<ApiResponse<List<TarjetaCreditoResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TarjetaCredito> tarjetas = tarjetaService.listarPorUsuario(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(
                tarjetas.stream().map(TarjetaCreditoResponse::fromDomain).toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarjeta por id")
    public ResponseEntity<ApiResponse<TarjetaCreditoResponse>> obtener(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        TarjetaCredito tarjeta = tarjetaService.obtenerPorIdValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(TarjetaCreditoResponse.fromDomain(tarjeta)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarjeta",
            description = "Permite modificar los datos de la tarjeta, incluido el saldo usado (cupoUsado).")
    public ResponseEntity<ApiResponse<TarjetaCreditoResponse>> actualizar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody TarjetaCreditoUpdateRequest request) {

        TarjetaCredito datos = TarjetaCredito.builder()
                .nombre(request.getNombre())
                .banco(request.getBanco())
                .ultimosCuatro(request.getUltimosCuatro())
                .cupoTotal(request.getCupoTotal())
                .cupoUsado(request.getCupoUsado())
                .diaCorte(request.getDiaCorte())
                .diaPago(request.getDiaPago())
                .color(request.getColor())
                .estado(request.getEstado())
                .build();

        TarjetaCredito actualizada = tarjetaService.actualizarValidado(id, datos, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(
                TarjetaCreditoResponse.fromDomain(actualizada),
                "Tarjeta actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarjeta",
            description = "Solo permitido si el cupo usado es 0. Si tiene gastos pendientes, cámbiala a estado BLOQUEADA o CANCELADA.")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        tarjetaService.eliminarValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Tarjeta eliminada exitosamente"));
    }
}
