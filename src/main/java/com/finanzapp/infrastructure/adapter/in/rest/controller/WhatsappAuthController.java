package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.SesionWhatsappService;
import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.model.SesionWhatsapp;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.in.DispositivoUseCase;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.WhatsappAuthEstadoResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.WhatsappRegistroRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.WhatsappVerificarCodigoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/whatsapp/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WhatsApp Auth", description = "Autenticacion de usuarios desde WhatsApp")
public class WhatsappAuthController {

    private final SesionWhatsappService sesionWhatsappService;
    private final DispositivoUseCase dispositivoUseCase;
    private final DispositivoRepositoryPort dispositivoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/estado")
    @Operation(summary = "Verificar estado de autenticacion",
            description = "Verifica si el usuario tiene sesion activa, cuenta existente y dispositivo registrado")
    public ResponseEntity<ApiResponse<WhatsappAuthEstadoResponse>> verificarEstado(
            @RequestParam String numeroWhatsapp) {

        Optional<SesionWhatsapp> sesion = sesionWhatsappService.verificarSesion(numeroWhatsapp);
        Optional<Dispositivo> dispositivo = dispositivoRepository.findByNumeroWhatsapp(numeroWhatsapp);

        boolean cuentaExiste = false;
        if (dispositivo.isPresent()) {
            cuentaExiste = usuarioRepository.findById(dispositivo.get().getUsuarioId()).isPresent();
        }

        WhatsappAuthEstadoResponse estado = WhatsappAuthEstadoResponse.builder()
                .sesionActiva(sesion.isPresent())
                .cuentaExiste(cuentaExiste)
                .dispositivoRegistrado(dispositivo.isPresent())
                .dispositivoVerificado(dispositivo.map(Dispositivo::isVerificado).orElse(false))
                .mensaje(construirMensajeEstado(sesion.isPresent(), cuentaExiste, dispositivo.isPresent()))
                .build();

        return ResponseEntity.ok(ApiResponse.success(estado));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario desde WhatsApp",
            description = "Registra un nuevo usuario, crea su dispositivo WhatsApp y genera un codigo OTP de verificacion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarDesdeWhatsapp(
            @RequestParam String numeroWhatsapp,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password) {

        if (usuarioRepository.existsByEmail(email)) {
            throw new DomainException("Ya existe un usuario con este email. Usa 'iniciar sesion' en lugar de registrarte.");
        }

        Optional<Dispositivo> dispositivoExistente = dispositivoRepository.findByNumeroWhatsapp(numeroWhatsapp);
        if (dispositivoExistente.isPresent()) {
            throw new DomainException("Este numero de WhatsApp ya esta vinculado a una cuenta.");
        }

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre(nombre)
                .email(email)
                .password(passwordEncoder.encode(password))
                .telefono(numeroWhatsapp)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuario registrado desde WhatsApp: {} ({})", usuario.getEmail(), numeroWhatsapp);

        Dispositivo dispositivo = dispositivoUseCase.registrar(
                usuario.getId(),
                numeroWhatsapp,
                "WhatsApp"
        );

        Map<String, Object> response = Map.of(
                "usuarioId", usuario.getId(),
                "codigoVerificacion", dispositivo.getCodigoVerificacion(),
                "mensaje", "Cuenta creada exitosamente. Se ha generado un codigo de verificacion."
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Usuario registrado. Verifica el codigo para activar tu sesion."));
    }

    @PostMapping("/solicitar-codigo")
    @Operation(summary = "Solicitar codigo OTP",
            description = "Genera un nuevo codigo de verificacion OTP para un dispositivo registrado")
    public ResponseEntity<ApiResponse<Map<String, Object>>> solicitarCodigo(
            @RequestParam String numeroWhatsapp) {

        Dispositivo dispositivo = dispositivoRepository.findByNumeroWhatsapp(numeroWhatsapp)
                .orElseThrow(() -> new DomainException(
                        "No se encontro un dispositivo con este numero. Registrate primero."));

        String codigo = dispositivoUseCase.generarNuevoCodigo(dispositivo.getId());

        Map<String, Object> response = Map.of(
                "codigoVerificacion", codigo,
                "expiraEnMinutos", 10,
                "mensaje", "Se ha generado un nuevo codigo de verificacion. Expira en 10 minutos."
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verificar-codigo")
    @Operation(summary = "Verificar codigo OTP y crear sesion",
            description = "Verifica el codigo OTP, activa el dispositivo y crea una sesion autenticada")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verificarCodigoYCrearSesion(
            @RequestParam String numeroWhatsapp,
            @RequestParam String codigo) {

        dispositivoUseCase.verificar(numeroWhatsapp, codigo);

        SesionWhatsapp sesion = sesionWhatsappService.crearSesion(numeroWhatsapp);

        Map<String, Object> response = Map.of(
                "sesionActiva", true,
                "mensaje", "Verificacion exitosa. Tu sesion esta activa y puedes empezar a gestionar tus finanzas."
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Sesion iniciada correctamente."));
    }

    @PostMapping("/cerrar-sesion")
    @Operation(summary = "Cerrar sesion WhatsApp",
            description = "Cierra la sesion activa del usuario en WhatsApp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cerrarSesion(
            @RequestParam String numeroWhatsapp) {

        sesionWhatsappService.cerrarSesion(numeroWhatsapp);

        Map<String, Object> response = Map.of(
                "sesionActiva", false,
                "mensaje", "Sesion cerrada correctamente. Escribe 'iniciar sesion' cuando quieras volver."
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String construirMensajeEstado(boolean sesionActiva, boolean cuentaExiste, boolean dispositivoRegistrado) {
        if (sesionActiva) {
            return "Tienes una sesion activa. Puedes gestionar tus finanzas.";
        }
        if (!cuentaExiste) {
            return "No tienes cuenta en FinanzApp. Escribe 'registrarme' para crear una.";
        }
        if (!dispositivoRegistrado) {
            return "Tienes cuenta pero este numero no esta vinculado. Contacta soporte.";
        }
        return "Tu sesion ha expirado. Escribe 'iniciar sesion' para autenticarte de nuevo.";
    }
}
