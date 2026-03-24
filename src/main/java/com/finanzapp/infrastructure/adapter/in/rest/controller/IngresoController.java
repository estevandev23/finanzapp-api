package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.Ingreso;
import com.finanzapp.application.service.IngresoService;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.BulkDeleteRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.BulkOperationResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ingreso.IngresoRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ingreso.IngresoUpdateRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ingresos")
@RequiredArgsConstructor
@Tag(name = "Ingresos", description = "Gestión de ingresos del usuario")
public class IngresoController {

    private final IngresoService ingresoService;

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

        Ingreso registrado = ingresoService.registrar(ingreso);
        return ResponseEntity.ok(ApiResponse.success(IngresoResponse.fromDomain(registrado), "Ingreso registrado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar ingresos", description = "Lista todos los ingresos del usuario autenticado")
    public ResponseEntity<ApiResponse<List<IngresoResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<IngresoResponse> ingresos = ingresoService.listarPorUsuario(userDetails.getId())
                .stream()
                .map(IngresoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(ingresos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ingreso", description = "Obtiene un ingreso por su ID")
    public ResponseEntity<ApiResponse<IngresoResponse>> obtener(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        Ingreso ingreso = ingresoService.obtenerPorIdValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(IngresoResponse.fromDomain(ingreso)));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar por periodo", description = "Lista los ingresos en un rango de fechas")
    public ResponseEntity<ApiResponse<List<IngresoResponse>>> listarPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<IngresoResponse> ingresos = ingresoService.listarPorPeriodo(userDetails.getId(), fechaInicio, fechaFin)
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

        List<IngresoResponse> ingresos = ingresoService.listarPorCategoria(userDetails.getId(), categoria)
                .stream()
                .map(IngresoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(ingresos));
    }

    @GetMapping("/total")
    @Operation(summary = "Obtener total", description = "Obtiene el total de ingresos del usuario")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotal(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        BigDecimal total = ingresoService.obtenerTotalIngresos(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/total/periodo")
    @Operation(summary = "Obtener total por periodo", description = "Obtiene el total de ingresos en un rango de fechas")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotalPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        BigDecimal total = ingresoService.obtenerTotalIngresosPorPeriodo(userDetails.getId(), fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ingreso", description = "Actualiza un ingreso existente")
    public ResponseEntity<ApiResponse<IngresoResponse>> actualizar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody IngresoUpdateRequest request) {

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

        Ingreso actualizado = ingresoService.actualizarValidado(id, ingreso, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(IngresoResponse.fromDomain(actualizado), "Ingreso actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ingreso", description = "Elimina un ingreso existente")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        ingresoService.eliminarValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Ingreso eliminado exitosamente"));
    }

    @PostMapping("/eliminar-lote")
    @Operation(summary = "Eliminar ingresos en lote", description = "Elimina múltiples ingresos por sus IDs")
    public ResponseEntity<ApiResponse<BulkOperationResponse>> eliminarLote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BulkDeleteRequest request) {

        int exitosos = 0;
        List<BulkOperationResponse.BulkError> errores = new ArrayList<>();

        for (UUID id : request.getIds()) {
            try {
                ingresoService.eliminarValidado(id, userDetails.getId());
                exitosos++;
            } catch (Exception e) {
                errores.add(new BulkOperationResponse.BulkError(id.toString(), e.getMessage()));
            }
        }

        BulkOperationResponse response = BulkOperationResponse.builder()
                .procesados(request.getIds().size())
                .exitosos(exitosos)
                .errores(errores)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response,
                String.format("Eliminación en lote completada: %d de %d eliminados", exitosos, request.getIds().size())));
    }

    @PostMapping("/lote")
    @Operation(summary = "Registrar ingresos en lote", description = "Registra múltiples ingresos en una sola operación")
    public ResponseEntity<ApiResponse<BulkOperationResponse>> registrarLote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<IngresoRequest> registros) {

        int exitosos = 0;
        List<BulkOperationResponse.BulkError> errores = new ArrayList<>();

        for (int i = 0; i < registros.size(); i++) {
            IngresoRequest request = registros.get(i);
            try {
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

                ingresoService.registrar(ingreso);
                exitosos++;
            } catch (Exception e) {
                errores.add(new BulkOperationResponse.BulkError(String.valueOf(i), e.getMessage()));
            }
        }

        BulkOperationResponse response = BulkOperationResponse.builder()
                .procesados(registros.size())
                .exitosos(exitosos)
                .errores(errores)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response,
                String.format("Registro en lote completado: %d de %d registrados", exitosos, registros.size())));
    }

    private void validarCategoria(IngresoRequest request) {
        if (request.getCategoria() == null && request.getCategoriaPersonalizadaId() == null) {
            throw new DomainException("Debe especificar una categoria o una categoria personalizada");
        }
    }
}
