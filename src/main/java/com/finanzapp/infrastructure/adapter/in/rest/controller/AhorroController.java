package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.Ahorro;
import com.finanzapp.application.service.AhorroService;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ahorro.AhorroRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ahorro.AhorroResponse;
import com.finanzapp.infrastructure.security.CustomUserDetails;
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
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ahorros")
@RequiredArgsConstructor
@Tag(name = "Ahorros", description = "Gestión de ahorros del usuario")
public class AhorroController {

    private final AhorroService ahorroService;

    @PostMapping
    @Operation(summary = "Registrar ahorro", description = "Registra un nuevo ahorro para el usuario autenticado")
    public ResponseEntity<ApiResponse<AhorroResponse>> registrar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AhorroRequest request) {

        Ahorro ahorro = Ahorro.builder()
                .usuarioId(userDetails.getId())
                .monto(request.getMonto())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .metaId(request.getMetaId())
                .ingresoId(request.getIngresoId())
                .build();

        Ahorro registrado = ahorroService.registrar(ahorro);
        return ResponseEntity.ok(ApiResponse.success(AhorroResponse.fromDomain(registrado), "Ahorro registrado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar ahorros", description = "Lista todos los ahorros del usuario autenticado")
    public ResponseEntity<ApiResponse<List<AhorroResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<AhorroResponse> ahorros = ahorroService.listarPorUsuario(userDetails.getId())
                .stream()
                .map(AhorroResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(ahorros));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ahorro", description = "Obtiene un ahorro por su ID")
    public ResponseEntity<ApiResponse<AhorroResponse>> obtener(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        Ahorro ahorro = ahorroService.obtenerPorIdValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(AhorroResponse.fromDomain(ahorro)));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar por periodo", description = "Lista los ahorros en un rango de fechas")
    public ResponseEntity<ApiResponse<List<AhorroResponse>>> listarPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<AhorroResponse> ahorros = ahorroService.listarPorPeriodo(userDetails.getId(), fechaInicio, fechaFin)
                .stream()
                .map(AhorroResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(ahorros));
    }

    @GetMapping("/meta/{metaId}")
    @Operation(summary = "Listar por meta", description = "Lista los ahorros asociados a una meta financiera")
    public ResponseEntity<ApiResponse<List<AhorroResponse>>> listarPorMeta(@PathVariable UUID metaId) {
        List<AhorroResponse> ahorros = ahorroService.listarPorMeta(metaId)
                .stream()
                .map(AhorroResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(ahorros));
    }

    @GetMapping("/total")
    @Operation(summary = "Obtener total", description = "Obtiene el total de ahorros del usuario")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotal(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        BigDecimal total = ahorroService.obtenerTotalAhorros(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/total/periodo")
    @Operation(summary = "Obtener total por periodo", description = "Obtiene el total de ahorros en un rango de fechas")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotalPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        BigDecimal total = ahorroService.obtenerTotalAhorrosPorPeriodo(userDetails.getId(), fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ahorro", description = "Actualiza un ahorro existente")
    public ResponseEntity<ApiResponse<AhorroResponse>> actualizar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody AhorroRequest request) {

        Ahorro ahorro = Ahorro.builder()
                .monto(request.getMonto())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .metaId(request.getMetaId())
                .build();

        Ahorro actualizado = ahorroService.actualizarValidado(id, ahorro, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(AhorroResponse.fromDomain(actualizado), "Ahorro actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ahorro", description = "Elimina un ahorro existente")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        ahorroService.eliminarValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Ahorro eliminado exitosamente"));
    }
}
