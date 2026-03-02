package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.port.in.GastoUseCase;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.gasto.GastoRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.gasto.GastoResponse;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import com.finanzapp.domain.exception.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/gastos")
@RequiredArgsConstructor
@Tag(name = "Gastos", description = "Gestión de gastos del usuario")
public class GastoController {

    private final GastoUseCase gastoUseCase;

    @PostMapping
    @Operation(summary = "Registrar gasto", description = "Registra un nuevo gasto para el usuario autenticado")
    public ResponseEntity<ApiResponse<GastoResponse>> registrar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GastoRequest request) {

        validarCategoria(request);

        Gasto gasto = Gasto.builder()
                .usuarioId(userDetails.getId())
                .monto(request.getMonto())
                .categoria(request.getCategoria())
                .categoriaPersonalizadaId(request.getCategoriaPersonalizadaId())
                .deudaId(request.getDeudaId())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .build();

        Gasto registrado = gastoUseCase.registrar(gasto);
        return ResponseEntity.ok(ApiResponse.success(GastoResponse.fromDomain(registrado), "Gasto registrado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar gastos", description = "Lista todos los gastos del usuario autenticado")
    public ResponseEntity<ApiResponse<List<GastoResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<GastoResponse> gastos = gastoUseCase.listarPorUsuario(userDetails.getId())
                .stream()
                .map(GastoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(gastos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener gasto", description = "Obtiene un gasto por su ID")
    public ResponseEntity<ApiResponse<GastoResponse>> obtener(@PathVariable UUID id) {
        Gasto gasto = gastoUseCase.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(GastoResponse.fromDomain(gasto)));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar por periodo", description = "Lista los gastos en un rango de fechas")
    public ResponseEntity<ApiResponse<List<GastoResponse>>> listarPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<GastoResponse> gastos = gastoUseCase.listarPorPeriodo(userDetails.getId(), fechaInicio, fechaFin)
                .stream()
                .map(GastoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(gastos));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Listar por categoría", description = "Lista los gastos de una categoría específica")
    public ResponseEntity<ApiResponse<List<GastoResponse>>> listarPorCategoria(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable CategoriaGasto categoria) {

        List<GastoResponse> gastos = gastoUseCase.listarPorCategoria(userDetails.getId(), categoria)
                .stream()
                .map(GastoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(gastos));
    }

    @GetMapping("/total")
    @Operation(summary = "Obtener total", description = "Obtiene el total de gastos del usuario")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotal(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        BigDecimal total = gastoUseCase.obtenerTotalGastos(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/total/periodo")
    @Operation(summary = "Obtener total por periodo", description = "Obtiene el total de gastos en un rango de fechas")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotalPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        BigDecimal total = gastoUseCase.obtenerTotalGastosPorPeriodo(userDetails.getId(), fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/desglose")
    @Operation(summary = "Obtener desglose por categoría", description = "Obtiene el desglose de gastos por categoría")
    public ResponseEntity<ApiResponse<Map<CategoriaGasto, BigDecimal>>> obtenerDesglose(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<CategoriaGasto, BigDecimal> desglose = gastoUseCase.obtenerDesglosePorCategoria(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(desglose));
    }

    @GetMapping("/desglose/periodo")
    @Operation(summary = "Obtener desglose por categoría en periodo", description = "Obtiene el desglose de gastos por categoría en un rango de fechas")
    public ResponseEntity<ApiResponse<Map<CategoriaGasto, BigDecimal>>> obtenerDesglosePorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        Map<CategoriaGasto, BigDecimal> desglose = gastoUseCase.obtenerDesglosePorCategoriaPorPeriodo(userDetails.getId(), fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success(desglose));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar gasto", description = "Actualiza un gasto existente")
    public ResponseEntity<ApiResponse<GastoResponse>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody GastoRequest request) {

        validarCategoria(request);

        Gasto gasto = Gasto.builder()
                .monto(request.getMonto())
                .categoria(request.getCategoria())
                .categoriaPersonalizadaId(request.getCategoriaPersonalizadaId())
                .deudaId(request.getDeudaId())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .build();

        Gasto actualizado = gastoUseCase.actualizar(id, gasto);
        return ResponseEntity.ok(ApiResponse.success(GastoResponse.fromDomain(actualizado), "Gasto actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar gasto", description = "Elimina un gasto existente")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        gastoUseCase.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Gasto eliminado exitosamente"));
    }

    private void validarCategoria(GastoRequest request) {
        if (request.getCategoria() == null && request.getCategoriaPersonalizadaId() == null) {
            throw new DomainException("Debe especificar una categoria o una categoria personalizada");
        }
    }
}
