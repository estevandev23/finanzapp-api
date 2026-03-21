package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.EstadoMeta;
import com.finanzapp.domain.model.MetaFinanciera;
import com.finanzapp.application.service.MetaFinancieraService;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.meta.MetaFinancieraRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.meta.MetaFinancieraResponse;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/metas")
@RequiredArgsConstructor
@Tag(name = "Metas Financieras", description = "Gestión de metas financieras del usuario")
public class MetaFinancieraController {

    private final MetaFinancieraService metaService;

    @PostMapping
    @Operation(summary = "Crear meta", description = "Crea una nueva meta financiera para el usuario autenticado")
    public ResponseEntity<ApiResponse<MetaFinancieraResponse>> crear(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MetaFinancieraRequest request) {

        MetaFinanciera meta = MetaFinanciera.builder()
                .usuarioId(userDetails.getId())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .montoObjetivo(request.getMontoObjetivo())
                .fechaLimite(request.getFechaLimite())
                .build();

        MetaFinanciera creada = metaService.crear(meta);
        return ResponseEntity.ok(ApiResponse.success(MetaFinancieraResponse.fromDomain(creada), "Meta financiera creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar metas", description = "Lista todas las metas financieras del usuario autenticado")
    public ResponseEntity<ApiResponse<List<MetaFinancieraResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<MetaFinancieraResponse> metas = metaService.listarPorUsuario(userDetails.getId())
                .stream()
                .map(MetaFinancieraResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(metas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener meta", description = "Obtiene una meta financiera por su ID")
    public ResponseEntity<ApiResponse<MetaFinancieraResponse>> obtener(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        MetaFinanciera meta = metaService.obtenerPorIdValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(MetaFinancieraResponse.fromDomain(meta)));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar por estado", description = "Lista las metas financieras por estado")
    public ResponseEntity<ApiResponse<List<MetaFinancieraResponse>>> listarPorEstado(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable EstadoMeta estado) {

        List<MetaFinancieraResponse> metas = metaService.listarPorEstado(userDetails.getId(), estado)
                .stream()
                .map(MetaFinancieraResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(metas));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar meta", description = "Actualiza una meta financiera existente")
    public ResponseEntity<ApiResponse<MetaFinancieraResponse>> actualizar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody MetaFinancieraRequest request) {

        MetaFinanciera meta = MetaFinanciera.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .montoObjetivo(request.getMontoObjetivo())
                .fechaLimite(request.getFechaLimite())
                .build();

        MetaFinanciera actualizada = metaService.actualizarValidado(id, meta, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(MetaFinancieraResponse.fromDomain(actualizada), "Meta financiera actualizada exitosamente"));
    }

    @PostMapping("/{id}/progreso")
    @Operation(summary = "Registrar progreso", description = "Registra un avance en la meta financiera creando un ahorro asociado")
    public ResponseEntity<ApiResponse<MetaFinancieraResponse>> registrarProgreso(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false) String descripcion) {

        MetaFinanciera meta = metaService.registrarProgreso(id, userDetails.getId(), monto, descripcion);
        return ResponseEntity.ok(ApiResponse.success(MetaFinancieraResponse.fromDomain(meta), "Progreso registrado exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de una meta financiera")
    public ResponseEntity<ApiResponse<MetaFinancieraResponse>> cambiarEstado(
            @PathVariable UUID id,
            @RequestParam EstadoMeta estado) {

        MetaFinanciera meta = metaService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.success(MetaFinancieraResponse.fromDomain(meta), "Estado cambiado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar meta", description = "Elimina una meta financiera existente")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        metaService.eliminarValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Meta financiera eliminada exitosamente"));
    }
}
