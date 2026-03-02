package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.VerificacionService;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.in.UsuarioUseCase;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verificacion/sms")
@RequiredArgsConstructor
@Tag(name = "Verificación SMS", description = "Verificación de número telefónico por SMS")
public class SmsVerificacionController {

    private final VerificacionService verificacionService;
    private final UsuarioUseCase usuarioUseCase;

    @PostMapping("/enviar")
    @Operation(summary = "Enviar código SMS", description = "Envía un código de verificación al teléfono del usuario")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enviarCodigo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Usuario usuario = usuarioUseCase.obtenerPorId(userDetails.getId());

        if (usuario.getTelefono() == null || usuario.getTelefono().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No tiene un número de teléfono registrado"));
        }

        UUID verificacionId = verificacionService.enviarCodigoVerificacionTelefono(
                usuario.getId(), usuario.getTelefono());

        Map<String, Object> data = Map.of(
                "verificacionId", verificacionId,
                "telefono", ocultarTelefono(usuario.getTelefono())
        );

        return ResponseEntity.ok(ApiResponse.success(data, "Código de verificación enviado"));
    }

    @PostMapping("/verificar")
    @Operation(summary = "Verificar código SMS", description = "Verifica el código enviado por SMS y marca el teléfono como verificado")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verificarCodigo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @NotBlank @Size(min = 6, max = 6) String codigo) {

        verificacionService.verificarTelefono(userDetails.getId(), codigo);

        Map<String, Boolean> data = Map.of("telefonoVerificado", true);
        return ResponseEntity.ok(ApiResponse.success(data, "Teléfono verificado exitosamente"));
    }

    /**
     * Oculta los digitos intermedios del telefono por seguridad.
     */
    private String ocultarTelefono(String telefono) {
        if (telefono.length() <= 4) return "****";
        return telefono.substring(0, 3) + "****" + telefono.substring(telefono.length() - 2);
    }
}
