package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.AuthService;
import com.finanzapp.application.service.VerificacionService;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.AuthResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.ForgotPasswordRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.ForgotPasswordResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.VerifyRecoveryCodeRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.LoginRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.OAuthRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.RegisterRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.ResetPasswordRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.Verify2FARequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.WhatsappLoginRequest;
import com.finanzapp.infrastructure.security.JwtService;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro de usuarios")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final VerificacionService verificacionService;
    private final UsuarioRepositoryPort usuarioRepository;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con email y contraseña. Si tiene 2FA activo, retorna requiere2FA=true")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = authService.authenticateUser(request.getEmail(), request.getPassword());

        if (usuario.isDosFactoresActivado()) {
            UUID verificacionId = verificacionService.enviarCodigo2FA(usuario.getId(), usuario.getEmail());
            AuthResponse response = AuthResponse.builder()
                    .requiere2FA(true)
                    .verificacionId(verificacionId)
                    .usuarioId(usuario.getId())
                    .email(usuario.getEmail())
                    .nombre(usuario.getNombre())
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response, "Se requiere verificación en dos pasos"));
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getId());
        String refreshToken = jwtService.generateRefreshToken(usuario.getEmail(), usuario.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Inicio de sesión exitoso"));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario en el sistema")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(request.getPassword())
                .telefono(request.getTelefono())
                .build();

        Usuario registrado = authService.registrar(usuario);
        String token = jwtService.generateToken(registrado.getEmail(), registrado.getId());
        String refreshToken = jwtService.generateRefreshToken(registrado.getEmail(), registrado.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .usuarioId(registrado.getId())
                .nombre(registrado.getNombre())
                .email(registrado.getEmail())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Usuario registrado exitosamente"));
    }

    @PostMapping("/login/whatsapp")
    @Operation(summary = "Iniciar sesión con WhatsApp", description = "Autentica un usuario mediante su dispositivo WhatsApp verificado")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWhatsapp(@Valid @RequestBody WhatsappLoginRequest request) {
        String token = authService.loginWhatsapp(request.getNumeroWhatsapp(), request.getCodigoVerificacion());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .usuarioId(jwtService.extractUserId(token))
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Inicio de sesión exitoso"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Genera un nuevo token de acceso usando el refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String newToken = authService.refreshToken(token);

        AuthResponse response = AuthResponse.builder()
                .token(newToken)
                .usuarioId(jwtService.extractUserId(newToken))
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Token refrescado exitosamente"));
    }

    @PostMapping("/oauth")
    @Operation(summary = "Login OAuth", description = "Autentica o registra un usuario mediante un proveedor OAuth (Google, GitHub)")
    public ResponseEntity<ApiResponse<AuthResponse>> loginOAuth(@Valid @RequestBody OAuthRequest request) {
        Usuario usuario = authService.loginOAuth(
                request.getProvider(), request.getProviderId(),
                request.getEmail(), request.getNombre()
        );

        if (usuario.isDosFactoresActivado()) {
            UUID verificacionId = verificacionService.enviarCodigo2FA(usuario.getId(), usuario.getEmail());
            AuthResponse response = AuthResponse.builder()
                    .requiere2FA(true)
                    .verificacionId(verificacionId)
                    .usuarioId(usuario.getId())
                    .email(usuario.getEmail())
                    .nombre(usuario.getNombre())
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response, "Se requiere verificacion en dos pasos"));
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getId());
        String refreshToken = jwtService.generateRefreshToken(usuario.getEmail(), usuario.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Autenticacion OAuth exitosa"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperacion de contrasena", description = "Envia un codigo de 6 digitos al correo para recuperar la contrasena")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        var usuarioOpt = usuarioRepository.findByEmail(request.getEmail());

        usuarioOpt.ifPresent(usuario ->
                verificacionService.enviarCodigoRecuperacionPassword(usuario.getId(), usuario.getEmail())
        );

        boolean tieneTelefono = usuarioOpt
                .map(u -> u.getTelefono() != null && !u.getTelefono().isBlank())
                .orElse(false);

        ForgotPasswordResponse response = ForgotPasswordResponse.builder()
                .tieneTelefono(tieneTelefono)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Si el correo esta registrado, recibiras un codigo de recuperacion"));
    }

    @PostMapping("/forgot-password/sms")
    @Operation(summary = "Enviar codigo de recuperacion por SMS", description = "Envia el codigo de recuperacion de contrasena por SMS al telefono registrado")
    public ResponseEntity<ApiResponse<Void>> forgotPasswordSms(@Valid @RequestBody ForgotPasswordRequest request) {
        usuarioRepository.findByEmail(request.getEmail()).ifPresent(usuario -> {
            if (usuario.getTelefono() != null && !usuario.getTelefono().isBlank()) {
                verificacionService.enviarCodigoRecuperacionPasswordSms(usuario.getId(), usuario.getTelefono());
            }
        });
        // Respuesta identica independientemente del resultado (seguridad)
        return ResponseEntity.ok(ApiResponse.success(null, "Si el numero esta registrado, recibiras un SMS con el codigo"));
    }

    @PostMapping("/verify-recovery-code")
    @Operation(summary = "Verificar codigo de recuperacion", description = "Valida el codigo sin marcarlo como usado para avanzar al paso de nueva contrasena")
    public ResponseEntity<ApiResponse<Void>> verifyRecoveryCode(@Valid @RequestBody VerifyRecoveryCodeRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.finanzapp.domain.exception.DomainException("Usuario no encontrado"));

        verificacionService.validarCodigoRecuperacion(usuario.getId(), request.getCodigo());

        return ResponseEntity.ok(ApiResponse.success(null, "Codigo valido"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Resetear contrasena", description = "Valida el codigo enviado por correo y establece la nueva contrasena")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.finanzapp.domain.exception.DomainException("Usuario no encontrado"));

        verificacionService.verificarCodigoRecuperacion(usuario.getId(), request.getCodigo());
        authService.cambiarPassword(usuario.getId(), request.getNuevaPassword());

        return ResponseEntity.ok(ApiResponse.success(null, "Contrasena actualizada exitosamente"));
    }

    @PostMapping("/verify-2fa")
    @Operation(summary = "Verificar código 2FA", description = "Verifica el código de autenticación en dos pasos y devuelve el token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> verify2FA(@Valid @RequestBody Verify2FARequest request) {
        verificacionService.verificarCodigo2FA(request.getUsuarioId(), request.getCodigo());

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new com.finanzapp.domain.exception.DomainException("Usuario no encontrado"));

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getId());
        String refreshToken = jwtService.generateRefreshToken(usuario.getEmail(), usuario.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Verificación exitosa"));
    }
}
