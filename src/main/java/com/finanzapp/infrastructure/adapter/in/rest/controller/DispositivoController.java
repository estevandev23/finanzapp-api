package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.port.in.DispositivoUseCase;
import com.finanzapp.domain.util.TelefonoUtils;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.dispositivo.DispositivoRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.dispositivo.DispositivoResponse;
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
@RequestMapping("/api/v1/dispositivos")
@RequiredArgsConstructor
@Tag(name = "Dispositivos", description = "Gestión de dispositivos WhatsApp del usuario")
public class DispositivoController {

    private final DispositivoUseCase dispositivoUseCase;

    @PostMapping
    @Operation(summary = "Registrar dispositivo", description = "Registra un nuevo dispositivo WhatsApp para el usuario")
    public ResponseEntity<ApiResponse<DispositivoResponse>> registrar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DispositivoRequest request) {

        Dispositivo dispositivo = dispositivoUseCase.registrar(
                userDetails.getId(),
                TelefonoUtils.normalizar(request.getNumeroWhatsapp()),
                request.getNombreDispositivo()
        );

        return ResponseEntity.ok(ApiResponse.success(
                DispositivoResponse.fromDomain(dispositivo),
                "Dispositivo registrado. Se ha enviado un código de verificación."
        ));
    }

    @PostMapping("/verificar")
    @Operation(summary = "Verificar dispositivo", description = "Verifica un dispositivo con el código recibido")
    public ResponseEntity<ApiResponse<DispositivoResponse>> verificar(
            @RequestParam String numeroWhatsapp,
            @RequestParam String codigo) {

        Dispositivo dispositivo = dispositivoUseCase.verificar(
                TelefonoUtils.normalizar(numeroWhatsapp), codigo);
        return ResponseEntity.ok(ApiResponse.success(
                DispositivoResponse.fromDomain(dispositivo),
                "Dispositivo verificado exitosamente"
        ));
    }

    @GetMapping
    @Operation(summary = "Listar dispositivos", description = "Lista todos los dispositivos del usuario")
    public ResponseEntity<ApiResponse<List<DispositivoResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<DispositivoResponse> dispositivos = dispositivoUseCase.listarPorUsuario(userDetails.getId())
                .stream()
                .map(DispositivoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dispositivos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener dispositivo", description = "Obtiene un dispositivo por su ID")
    public ResponseEntity<ApiResponse<DispositivoResponse>> obtener(@PathVariable UUID id) {
        Dispositivo dispositivo = dispositivoUseCase.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(DispositivoResponse.fromDomain(dispositivo)));
    }

    @PostMapping("/{id}/nuevo-codigo")
    @Operation(summary = "Generar nuevo código", description = "Genera un nuevo código de verificación para el dispositivo")
    public ResponseEntity<ApiResponse<String>> generarNuevoCodigo(@PathVariable UUID id) {
        String codigo = dispositivoUseCase.generarNuevoCodigo(id);
        return ResponseEntity.ok(ApiResponse.success(codigo, "Nuevo código generado"));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar dispositivo", description = "Desactiva un dispositivo")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable UUID id) {
        dispositivoUseCase.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Dispositivo desactivado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar dispositivo", description = "Elimina un dispositivo")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        dispositivoUseCase.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Dispositivo eliminado exitosamente"));
    }
}
