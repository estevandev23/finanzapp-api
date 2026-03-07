package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.Ingreso;
import com.finanzapp.domain.port.in.IngresoUseCase;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ingreso.IngresoRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ingreso.IngresoResponse;
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
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ingresos")
@RequiredArgsConstructor
@Tag(name = "Ingresos", description = "Gestión de ingresos del usuario")
public class IngresoController {

    private final IngresoUseCase ingresoUseCase;

    @PostMapping
    @Operation(summary = "Registrar ingreso", description = "Registra un nuevo ingreso para el usuario autenticado")
    public ResponseEntity<ApiResponse<IngresoResponse>> registrar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody IngresoRequest request) {

        validarCategoria(request);

        Ingreso ingreso = Ingreso.builder()
                .usuarioId(userDetails.getId())
                .monto(request.getMonto())
                .categoria(request.getCategoria())
                .categoriaPersonalizadaId(request.getCategoriaPersonalizadaId())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .montoAhorro(request.getMontoAhorro())
                .metaId(request.getMetaId())
                .prestamoId(request.getPrestamoId())
                .metodoPago(request.getMetodoPago())
                .build();

        Ingreso registrado = ingresoUseCase.registrar(ingreso);
        return ResponseEntity.ok(ApiResponse.success(IngresoResponse.fromDomain(registrado), "Ingreso registrado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar ingresos", description = "Lista todos los ingresos del usuario autenticado")
    public ResponseEntity<ApiResponse<List<IngresoResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<IngresoResponse> ingresos = ingresoUseCase.listarPorUsuario(userDetails.getId())
                .stream()
                .map(IngresoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(ingresos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ingreso", description = "Obtiene un ingreso por su ID")
    public ResponseEntity<ApiResponse<IngresoResponse>> obtener(@PathVariable UUID id) {
        Ingreso ingreso = ingresoUseCase.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(IngresoResponse.fromDomain(ingreso)));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar por periodo", description = "Lista los ingresos en un rango de fechas")
    public ResponseEntity<ApiResponse<List<IngresoResponse>>> listarPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<IngresoResponse> ingresos = ingresoUseCase.listarPorPeriodo(userDetails.getId(), fechaInicio, fechaFin)
                .stream()
                .map(IngresoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(ingresos));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Listar por categoría", description = "Lista los ingresos de una categoría específica")
    public ResponseEntity<ApiResponse<List<IngresoResponse>>> listarPorCategoria(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable CategoriaIngreso categoria) {

        List<IngresoResponse> ingresos = ingresoUseCase.listarPorCategoria(userDetails.getId(), categoria)
                .stream()
                .map(IngresoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(ingresos));
    }

    @GetMapping("/total")
    @Operation(summary = "Obtener total", description = "Obtiene el total de ingresos del usuario")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotal(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        BigDecimal total = ingresoUseCase.obtenerTotalIngresos(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/total/periodo")
    @Operation(summary = "Obtener total por periodo", description = "Obtiene el total de ingresos en un rango de fechas")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotalPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        BigDecimal total = ingresoUseCase.obtenerTotalIngresosPorPeriodo(userDetails.getId(), fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ingreso", description = "Actualiza un ingreso existente")
    public ResponseEntity<ApiResponse<IngresoResponse>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody IngresoRequest request) {

        validarCategoria(request);

        Ingreso ingreso = Ingreso.builder()
                .monto(request.getMonto())
                .categoria(request.getCategoria())
                .categoriaPersonalizadaId(request.getCategoriaPersonalizadaId())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .montoAhorro(request.getMontoAhorro())
                .metaId(request.getMetaId())
                .prestamoId(request.getPrestamoId())
                .metodoPago(request.getMetodoPago())
                .build();

        Ingreso actualizado = ingresoUseCase.actualizar(id, ingreso);
        return ResponseEntity.ok(ApiResponse.success(IngresoResponse.fromDomain(actualizado), "Ingreso actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ingreso", description = "Elimina un ingreso existente")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        ingresoUseCase.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Ingreso eliminado exitosamente"));
    }

    private void validarCategoria(IngresoRequest request) {
        if (request.getCategoria() == null && request.getCategoriaPersonalizadaId() == null) {
            throw new DomainException("Debe especificar una categoria o una categoria personalizada");
        }
    }
}
