package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.AbonoDeuda;
import com.finanzapp.domain.model.Deuda;
import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.TipoDeuda;
import com.finanzapp.application.service.DeudaService;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.BulkDeleteRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.BulkOperationResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.deuda.AbonoRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.deuda.AbonoResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.deuda.DeudaRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.deuda.DeudaUpdateRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.deuda.DeudaResponse;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/deudas")
@RequiredArgsConstructor
@Tag(name = "Deudas y Prestamos", description = "Gestion de deudas propias y dinero prestado")
public class DeudaController {

    private final DeudaService deudaService;

    @PostMapping
    @Operation(summary = "Crear deuda o prestamo")
    public ResponseEntity<ApiResponse<DeudaResponse>> crear(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DeudaRequest request) {

        Deuda deuda = Deuda.builder()
                .usuarioId(userDetails.getId())
                .tipo(request.getTipo())
                .descripcion(request.getDescripcion())
                .entidad(request.getEntidad())
                .categoria(request.getCategoria())
                .categoriaPersonalizadaId(
                        request.getCategoriaPersonalizadaId() != null
                                ? java.util.UUID.fromString(request.getCategoriaPersonalizadaId())
                                : null)
                .montoTotal(request.getMontoTotal())
                .fechaInicio(request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now())
                .fechaLimite(request.getFechaLimite())
                .tarjetaId(request.getTarjetaId() != null ? java.util.UUID.fromString(request.getTarjetaId()) : null)
                .build();

        Deuda creada = deudaService.registrar(deuda);
        return ResponseEntity.ok(ApiResponse.success(DeudaResponse.fromDomain(creada), "Registro creado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar deudas y prestamos del usuario")
    public ResponseEntity<ApiResponse<List<DeudaResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<DeudaResponse> deudas = deudaService.listarPorUsuario(userDetails.getId()).stream()
                .map(DeudaResponse::fromDomain).toList();
        return ResponseEntity.ok(ApiResponse.success(deudas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener deuda por ID")
    public ResponseEntity<ApiResponse<DeudaResponse>> obtenerPorId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id) {
        Deuda deuda = deudaService.obtenerPorIdValidado(java.util.UUID.fromString(id), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(DeudaResponse.fromDomain(deuda)));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar por tipo (DEUDA o PRESTAMO)")
    public ResponseEntity<ApiResponse<List<DeudaResponse>>> listarPorTipo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable TipoDeuda tipo) {

        List<DeudaResponse> deudas = deudaService.listarPorTipo(userDetails.getId(), tipo).stream()
                .map(DeudaResponse::fromDomain).toList();
        return ResponseEntity.ok(ApiResponse.success(deudas));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar por estado")
    public ResponseEntity<ApiResponse<List<DeudaResponse>>> listarPorEstado(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable EstadoDeuda estado) {

        List<DeudaResponse> deudas = deudaService.listarPorEstado(userDetails.getId(), estado).stream()
                .map(DeudaResponse::fromDomain).toList();
        return ResponseEntity.ok(ApiResponse.success(deudas));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar deuda o prestamo")
    public ResponseEntity<ApiResponse<DeudaResponse>> actualizar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody DeudaUpdateRequest request) {

        Deuda datos = Deuda.builder()
                .descripcion(request.getDescripcion())
                .entidad(request.getEntidad())
                .categoria(request.getCategoria())
                .categoriaPersonalizadaId(
                        request.getCategoriaPersonalizadaId() != null
                                ? java.util.UUID.fromString(request.getCategoriaPersonalizadaId())
                                : null)
                .montoTotal(request.getMontoTotal())
                .fechaLimite(request.getFechaLimite())
                .tarjetaId(request.getTarjetaId() != null ? java.util.UUID.fromString(request.getTarjetaId()) : null)
                .build();

        Deuda actualizada = deudaService.actualizarValidado(java.util.UUID.fromString(id), datos, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(DeudaResponse.fromDomain(actualizada), "Registro actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar deuda o prestamo")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id) {
        deudaService.eliminarValidado(java.util.UUID.fromString(id), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Registro eliminado exitosamente"));
    }

    @PostMapping("/eliminar-lote")
    @Operation(summary = "Eliminar deudas/prestamos en lote", description = "Elimina múltiples deudas o prestamos por sus IDs")
    public ResponseEntity<ApiResponse<BulkOperationResponse>> eliminarLote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BulkDeleteRequest request) {

        int exitosos = 0;
        List<BulkOperationResponse.BulkError> errores = new ArrayList<>();

        for (java.util.UUID id : request.getIds()) {
            try {
                deudaService.eliminarValidado(id, userDetails.getId());
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
    @Operation(summary = "Registrar deudas/prestamos en lote", description = "Registra múltiples deudas o prestamos en una sola operación")
    public ResponseEntity<ApiResponse<BulkOperationResponse>> registrarLote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<DeudaRequest> registros) {

        int exitosos = 0;
        List<BulkOperationResponse.BulkError> errores = new ArrayList<>();

        for (int i = 0; i < registros.size(); i++) {
            DeudaRequest request = registros.get(i);
            try {
                Deuda deuda = Deuda.builder()
                        .usuarioId(userDetails.getId())
                        .tipo(request.getTipo())
                        .descripcion(request.getDescripcion())
                        .entidad(request.getEntidad())
                        .categoria(request.getCategoria())
                        .categoriaPersonalizadaId(
                                request.getCategoriaPersonalizadaId() != null
                                        ? java.util.UUID.fromString(request.getCategoriaPersonalizadaId())
                                        : null)
                        .montoTotal(request.getMontoTotal())
                        .fechaInicio(request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now())
                        .fechaLimite(request.getFechaLimite())
                        .tarjetaId(request.getTarjetaId() != null ? java.util.UUID.fromString(request.getTarjetaId()) : null)
                        .build();

                deudaService.registrar(deuda);
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

    @PostMapping("/{id}/abonos")
    @Operation(summary = "Registrar abono a deuda o prestamo")
    public ResponseEntity<ApiResponse<AbonoResponse>> registrarAbono(
            @PathVariable String id,
            @Valid @RequestBody AbonoRequest request) {

        MetodoPago metodoPago = MetodoPago.valueOf(request.getMetodoPago());
        AbonoDeuda abono = deudaService.registrarAbono(
                java.util.UUID.fromString(id), request.getMonto(), request.getDescripcion(), metodoPago);
        return ResponseEntity.ok(ApiResponse.success(AbonoResponse.fromDomain(abono), "Abono registrado exitosamente"));
    }

    @GetMapping("/{id}/abonos")
    @Operation(summary = "Listar historial de abonos de una deuda")
    public ResponseEntity<ApiResponse<List<AbonoResponse>>> listarAbonos(@PathVariable String id) {
        List<AbonoResponse> abonos = deudaService.listarAbonos(java.util.UUID.fromString(id)).stream()
                .map(AbonoResponse::fromDomain).toList();
        return ResponseEntity.ok(ApiResponse.success(abonos));
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen de deudas y prestamos del usuario")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> obtenerResumen(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        BigDecimal totalDeudas = deudaService.obtenerTotalDeudas(userDetails.getId());
        BigDecimal totalPrestamos = deudaService.obtenerTotalPrestamos(userDetails.getId());
        BigDecimal abonosRecibidos = deudaService.obtenerTotalAbonosPrestamosRecibidos(userDetails.getId());

        Map<String, BigDecimal> resumen = Map.of(
                "totalDeudas", totalDeudas,
                "totalPrestamos", totalPrestamos,
                "abonosRecibidos", abonosRecibidos
        );

        return ResponseEntity.ok(ApiResponse.success(resumen));
    }
}
