package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.model.GastoMetodoPago;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.application.service.GastoService;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.BulkDeleteRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.BulkOperationResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.gasto.GastoRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.gasto.GastoUpdateRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.gasto.GastoResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.gasto.MetodoPagoDetalleRequest;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/gastos")
@RequiredArgsConstructor
@Tag(name = "Gastos", description = "Gestión de gastos del usuario")
public class GastoController {

    private final GastoService gastoService;

    @PostMapping
    @Operation(summary = "Registrar gasto", description = "Registra un nuevo gasto para el usuario autenticado")
    public ResponseEntity<ApiResponse<GastoResponse>> registrar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GastoRequest request) {

        validarCategoria(request);

        List<GastoMetodoPago> metodos = null;
        if (request.getMetodosPago() != null && !request.getMetodosPago().isEmpty()) {
            metodos = request.getMetodosPago().stream()
                    .map(m -> GastoMetodoPago.builder()
                            .metodo(m.getMetodo())
                            .monto(m.getMonto())
                            .build())
                    .toList();
        } else if (request.getMetodoPago() != null) {
            metodos = List.of(GastoMetodoPago.builder()
                    .metodo(request.getMetodoPago())
                    .monto(request.getMonto())
                    .build());
        }

        Gasto gasto = Gasto.builder()
                .usuarioId(userDetails.getId())
                .monto(request.getMonto())
                .categoria(request.getCategoria())
                .categoriaPersonalizadaId(request.getCategoriaPersonalizadaId())
                .deudaId(request.getDeudaId())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .metodosPago(metodos)
                .build();

        Gasto registrado = gastoService.registrar(gasto);
        return ResponseEntity.ok(ApiResponse.success(GastoResponse.fromDomain(registrado), "Gasto registrado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar gastos", description = "Lista todos los gastos del usuario autenticado")
    public ResponseEntity<ApiResponse<List<GastoResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<GastoResponse> gastos = gastoService.listarPorUsuario(userDetails.getId())
                .stream()
                .map(GastoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(gastos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener gasto", description = "Obtiene un gasto por su ID")
    public ResponseEntity<ApiResponse<GastoResponse>> obtener(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        Gasto gasto = gastoService.obtenerPorIdValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(GastoResponse.fromDomain(gasto)));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar por periodo", description = "Lista los gastos en un rango de fechas")
    public ResponseEntity<ApiResponse<List<GastoResponse>>> listarPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<GastoResponse> gastos = gastoService.listarPorPeriodo(userDetails.getId(), fechaInicio, fechaFin)
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

        List<GastoResponse> gastos = gastoService.listarPorCategoria(userDetails.getId(), categoria)
                .stream()
                .map(GastoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(gastos));
    }

    @GetMapping("/total")
    @Operation(summary = "Obtener total", description = "Obtiene el total de gastos del usuario")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotal(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        BigDecimal total = gastoService.obtenerTotalGastos(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/total/periodo")
    @Operation(summary = "Obtener total por periodo", description = "Obtiene el total de gastos en un rango de fechas")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerTotalPorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        BigDecimal total = gastoService.obtenerTotalGastosPorPeriodo(userDetails.getId(), fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/desglose")
    @Operation(summary = "Obtener desglose por categoría", description = "Obtiene el desglose de gastos por categoría")
    public ResponseEntity<ApiResponse<Map<CategoriaGasto, BigDecimal>>> obtenerDesglose(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<CategoriaGasto, BigDecimal> desglose = gastoService.obtenerDesglosePorCategoria(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(desglose));
    }

    @GetMapping("/desglose/periodo")
    @Operation(summary = "Obtener desglose por categoría en periodo", description = "Obtiene el desglose de gastos por categoría en un rango de fechas")
    public ResponseEntity<ApiResponse<Map<CategoriaGasto, BigDecimal>>> obtenerDesglosePorPeriodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        Map<CategoriaGasto, BigDecimal> desglose = gastoService.obtenerDesglosePorCategoriaPorPeriodo(userDetails.getId(), fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success(desglose));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar gasto", description = "Actualiza un gasto existente")
    public ResponseEntity<ApiResponse<GastoResponse>> actualizar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody GastoUpdateRequest request) {

        List<GastoMetodoPago> metodos = null;
        if (request.getMetodosPago() != null && !request.getMetodosPago().isEmpty()) {
            metodos = request.getMetodosPago().stream()
                    .map(m -> GastoMetodoPago.builder()
                            .metodo(m.getMetodo())
                            .monto(m.getMonto())
                            .build())
                    .toList();
        } else if (request.getMetodoPago() != null) {
            metodos = List.of(GastoMetodoPago.builder()
                    .metodo(request.getMetodoPago())
                    .monto(request.getMonto())
                    .build());
        }

        Gasto gasto = Gasto.builder()
                .monto(request.getMonto())
                .categoria(request.getCategoria())
                .categoriaPersonalizadaId(request.getCategoriaPersonalizadaId())
                .deudaId(request.getDeudaId())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .metodosPago(metodos)
                .build();

        Gasto actualizado = gastoService.actualizarValidado(id, gasto, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(GastoResponse.fromDomain(actualizado), "Gasto actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar gasto", description = "Elimina un gasto existente")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        gastoService.eliminarValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Gasto eliminado exitosamente"));
    }

    @PostMapping("/eliminar-lote")
    @Operation(summary = "Eliminar gastos en lote", description = "Elimina múltiples gastos por sus IDs")
    public ResponseEntity<ApiResponse<BulkOperationResponse>> eliminarLote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BulkDeleteRequest request) {

        int exitosos = 0;
        List<BulkOperationResponse.BulkError> errores = new ArrayList<>();

        for (UUID id : request.getIds()) {
            try {
                gastoService.eliminarValidado(id, userDetails.getId());
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
    @Operation(summary = "Registrar gastos en lote", description = "Registra múltiples gastos en una sola operación")
    public ResponseEntity<ApiResponse<BulkOperationResponse>> registrarLote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<GastoRequest> registros) {

        int exitosos = 0;
        List<BulkOperationResponse.BulkError> errores = new ArrayList<>();

        for (int i = 0; i < registros.size(); i++) {
            GastoRequest request = registros.get(i);
            try {
                validarCategoria(request);

                List<GastoMetodoPago> metodos = null;
                if (request.getMetodosPago() != null && !request.getMetodosPago().isEmpty()) {
                    metodos = request.getMetodosPago().stream()
                            .map(m -> GastoMetodoPago.builder()
                                    .metodo(m.getMetodo())
                                    .monto(m.getMonto())
                                    .build())
                            .toList();
                } else if (request.getMetodoPago() != null) {
                    metodos = List.of(GastoMetodoPago.builder()
                            .metodo(request.getMetodoPago())
                            .monto(request.getMonto())
                            .build());
                }

                Gasto gasto = Gasto.builder()
                        .usuarioId(userDetails.getId())
                        .monto(request.getMonto())
                        .categoria(request.getCategoria())
                        .categoriaPersonalizadaId(request.getCategoriaPersonalizadaId())
                        .deudaId(request.getDeudaId())
                        .descripcion(request.getDescripcion())
                        .fecha(request.getFecha())
                        .metodosPago(metodos)
                        .build();

                gastoService.registrar(gasto);
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

    private void validarCategoria(GastoRequest request) {
        if (request.getCategoria() == null && request.getCategoriaPersonalizadaId() == null) {
            throw new DomainException("Debe especificar una categoria o una categoria personalizada");
        }
    }
}
