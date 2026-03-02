package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.VerificacionService;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.in.UsuarioUseCase;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.usuario.PasswordChangeRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.usuario.UsuarioResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.usuario.UsuarioUpdateRequest;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión del perfil del usuario")
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;
    private final VerificacionService verificacionService;

    @GetMapping("/perfil")
    @Operation(summary = "Obtener perfil", description = "Obtiene los datos del usuario autenticado")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPerfil(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Usuario usuario = usuarioUseCase.obtenerPorId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(UsuarioResponse.fromDomain(usuario)));
    }

    @PutMapping("/perfil")
    @Operation(summary = "Actualizar perfil", description = "Actualiza los datos del usuario autenticado")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarPerfil(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UsuarioUpdateRequest request) {

        Usuario datosActualizados = Usuario.builder()
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .build();

        Usuario actualizado = usuarioUseCase.actualizar(userDetails.getId(), datosActualizados);
        return ResponseEntity.ok(ApiResponse.success(UsuarioResponse.fromDomain(actualizado), "Perfil actualizado exitosamente"));
    }

    @PutMapping("/password")
    @Operation(summary = "Cambiar contraseña", description = "Cambia la contraseña del usuario autenticado")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request) {

        usuarioUseCase.cambiarPassword(userDetails.getId(), request.getPasswordActual(), request.getNuevaPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Contraseña actualizada exitosamente"));
    }

    @PostMapping("/2fa/toggle")
    @Operation(summary = "Activar/Desactivar 2FA", description = "Activa o desactiva la autenticación en dos pasos por email")
    public ResponseEntity<ApiResponse<UsuarioResponse>> toggle2FA(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam boolean activar) {

        verificacionService.toggle2FA(userDetails.getId(), activar);
        Usuario usuario = usuarioUseCase.obtenerPorId(userDetails.getId());
        String mensaje = activar ? "Autenticación en dos pasos activada" : "Autenticación en dos pasos desactivada";
        return ResponseEntity.ok(ApiResponse.success(UsuarioResponse.fromDomain(usuario), mensaje));
    }
}
