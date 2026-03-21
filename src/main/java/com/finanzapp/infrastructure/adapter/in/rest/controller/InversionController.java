package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.EstadoInversion;
import com.finanzapp.domain.model.Inversion;
import com.finanzapp.application.service.InversionService;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.inversion.InversionRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.inversion.InversionResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.inversion.RegistrarRetornoRequest;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/inversiones")
@RequiredArgsConstructor
@Tag(name = "Inversiones", description = "Gestión de inversiones del usuario")
public class InversionController {

    private final InversionService inversionService;

    @PostMapping
    @Operation(summary = "Crear inversión", description = "Crea una nueva inversión y registra automáticamente un gasto asociado")
    public ResponseEntity<ApiResponse<InversionResponse>> crear(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InversionRequest request) {

        Inversion inversion = Inversion.builder()
                .usuarioId(userDetails.getId())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .retornoEsperado(request.getRetornoEsperado())
                .fechaInversion(request.getFechaInversion())
                .build();

        Inversion creada = inversionService.crear(inversion);
        return ResponseEntity.ok(ApiResponse.success(InversionResponse.fromDomain(creada), "Inversión creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar inversiones", description = "Lista todas las inversiones del usuario autenticado")
    public ResponseEntity<ApiResponse<List<InversionResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) EstadoInversion estado) {

        List<InversionResponse> inversiones;
        if (estado != null) {
            inversiones = inversionService.listarPorEstado(userDetails.getId(), estado)
                    .stream()
                    .map(InversionResponse::fromDomain)
                    .collect(Collectors.toList());
        } else {
            inversiones = inversionService.listarPorUsuario(userDetails.getId())
                    .stream()
                    .map(InversionResponse::fromDomain)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success(inversiones));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener inversión", description = "Obtiene una inversión por su ID")
    public ResponseEntity<ApiResponse<InversionResponse>> obtener(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        Inversion inversion = inversionService.obtenerPorIdValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(InversionResponse.fromDomain(inversion)));
    }

    @PostMapping("/{id}/retorno")
    @Operation(summary = "Registrar retorno", description = "Registra el retorno real de una inversión y crea el ingreso correspondiente")
    public ResponseEntity<ApiResponse<InversionResponse>> registrarRetorno(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarRetornoRequest request) {

        Inversion inversion = inversionService.registrarRetorno(id, request.getRetornoReal(), request.getFechaRetorno());
        return ResponseEntity.ok(ApiResponse.success(InversionResponse.fromDomain(inversion), "Retorno registrado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar inversión", description = "Elimina una inversión y sus registros asociados (gasto e ingreso)")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        inversionService.eliminarValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Inversión eliminada exitosamente"));
    }
}
