package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.SesionWhatsappService;
import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.util.TelefonoUtils;
import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.model.SesionWhatsapp;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.model.WhatsappLoginToken;
import com.finanzapp.domain.port.in.DispositivoUseCase;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import com.finanzapp.domain.port.out.WhatsappLoginTokenRepositoryPort;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.WhatsappAuthEstadoResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.WhatsappRegistroRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.auth.WhatsappVerificarCodigoRequest;
import com.finanzapp.infrastructure.adapter.out.messaging.EvolutionApiService;
import com.finanzapp.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/whatsapp/auth")
@Slf4j
@Tag(name = "WhatsApp Auth", description = "Autenticacion de usuarios desde WhatsApp")
public class WhatsappAuthController {

    private final SesionWhatsappService sesionWhatsappService;
    private final DispositivoUseCase dispositivoUseCase;
    private final DispositivoRepositoryPort dispositivoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final WhatsappLoginTokenRepositoryPort whatsappLoginTokenRepository;
    private final EvolutionApiService evolutionApiService;
    private final JwtService jwtService;
    private final String frontendBaseUrl;

    public WhatsappAuthController(
            SesionWhatsappService sesionWhatsappService,
            DispositivoUseCase dispositivoUseCase,
            DispositivoRepositoryPort dispositivoRepository,
            UsuarioRepositoryPort usuarioRepository,
            PasswordEncoder passwordEncoder,
            WhatsappLoginTokenRepositoryPort whatsappLoginTokenRepository,
            EvolutionApiService evolutionApiService,
            JwtService jwtService,
            @Value("${app.frontend.url:https://finanzappweb.estevanv.dev}") String frontendBaseUrl) {
        this.sesionWhatsappService = sesionWhatsappService;
        this.dispositivoUseCase = dispositivoUseCase;
        this.dispositivoRepository = dispositivoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.whatsappLoginTokenRepository = whatsappLoginTokenRepository;
        this.evolutionApiService = evolutionApiService;
        this.jwtService = jwtService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    private String normalizarTelefono(String numero) {
        return TelefonoUtils.normalizar(numero);
    }

    @GetMapping("/estado")
    @Operation(summary = "Verificar estado de autenticacion",
            description = "Verifica si el usuario tiene sesion activa, cuenta existente y dispositivo registrado")
    public ResponseEntity<ApiResponse<WhatsappAuthEstadoResponse>> verificarEstado(
            @RequestParam String numeroWhatsapp) {

        String telefono = normalizarTelefono(numeroWhatsapp);

        Optional<SesionWhatsapp> sesion = sesionWhatsappService.verificarSesion(telefono);
        Optional<Dispositivo> dispositivo = dispositivoRepository.findByNumeroWhatsapp(telefono);

        boolean cuentaExiste = false;
        if (dispositivo.isPresent()) {
            cuentaExiste = usuarioRepository.findById(dispositivo.get().getUsuarioId()).isPresent();
        }
        if (!cuentaExiste) {
            cuentaExiste = usuarioRepository.findByTelefono(telefono).isPresent();
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

    @GetMapping("/obtener-token")
    @Operation(summary = "Obtener token JWT de sesion activa",
            description = "Retorna el token JWT de una sesion WhatsApp activa. Solo para uso interno del servidor MCP.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerToken(
            @RequestParam String numeroWhatsapp) {

        String telefono = normalizarTelefono(numeroWhatsapp);

        SesionWhatsapp sesion = sesionWhatsappService.verificarSesion(telefono)
                .orElseThrow(() -> new DomainException("No hay sesion WhatsApp activa para este numero."));

        sesionWhatsappService.actualizarActividad(telefono);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "token", sesion.getToken(),
                "usuarioId", sesion.getUsuarioId().toString()
        )));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario desde WhatsApp",
            description = "Registra un nuevo usuario o vincula una cuenta existente por email, crea su dispositivo WhatsApp y genera un codigo OTP de verificacion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarDesdeWhatsapp(
            @RequestParam String numeroWhatsapp,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password) {

        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("El nombre es requerido.");
        }
        if (email == null || email.isBlank()) {
            throw new DomainException("El email es requerido.");
        }
        if (password == null || password.isBlank()) {
            throw new DomainException("La contrasena es requerida.");
        }

        String telefono = normalizarTelefono(numeroWhatsapp);

        Optional<Dispositivo> dispositivoExistente = dispositivoRepository.findByNumeroWhatsapp(telefono);
        if (dispositivoExistente.isPresent()) {
            throw new DomainException("Este numero de WhatsApp ya esta vinculado a una cuenta.");
        }

        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(email);
        Usuario usuario;

        if (usuarioExistente.isPresent()) {
            usuario = usuarioExistente.get();

            if (usuario.getTelefono() != null && !usuario.getTelefono().isBlank()) {
                throw new DomainException("Ya existe un usuario con este email y tiene un numero asociado. Usa 'iniciar sesion' en lugar de registrarte.");
            }

            usuario.setTelefono(telefono);
            usuario.setFechaActualizacion(LocalDateTime.now());
            usuarioRepository.save(usuario);
            log.info("Cuenta existente vinculada a WhatsApp: {} ({})", email, telefono);
        } else {
            usuario = Usuario.builder()
                    .id(UUID.randomUUID())
                    .nombre(nombre)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .telefono(telefono)
                    .activo(true)
                    .fechaCreacion(LocalDateTime.now())
                    .fechaActualizacion(LocalDateTime.now())
                    .build();

            usuarioRepository.save(usuario);
            log.info("Usuario registrado desde WhatsApp: {} ({})", email, telefono);
        }

        Dispositivo dispositivo = dispositivoUseCase.registrar(
                usuario.getId(),
                telefono,
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
            description = "Genera un nuevo codigo de verificacion OTP. Si no existe dispositivo pero la cuenta si, lo crea automaticamente")
    public ResponseEntity<ApiResponse<Map<String, Object>>> solicitarCodigo(
            @RequestParam String numeroWhatsapp) {

        String telefono = normalizarTelefono(numeroWhatsapp);

        Optional<Dispositivo> dispositivoOpt = dispositivoRepository.findByNumeroWhatsapp(telefono);
        String codigo;

        if (dispositivoOpt.isPresent()) {
            codigo = dispositivoUseCase.generarNuevoCodigo(dispositivoOpt.get().getId());
        } else {
            Usuario usuario = usuarioRepository.findByTelefono(telefono)
                    .orElseThrow(() -> new DomainException(
                            "No se encontro una cuenta con este numero. Registrate primero."));

            Dispositivo nuevoDispositivo = dispositivoUseCase.registrar(
                    usuario.getId(), telefono, "WhatsApp");
            codigo = nuevoDispositivo.getCodigoVerificacion();
            log.info("Dispositivo creado automaticamente para usuario: {} ({})", usuario.getEmail(), telefono);
        }

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

        String telefono = normalizarTelefono(numeroWhatsapp);

        dispositivoUseCase.verificar(telefono, codigo);

        SesionWhatsapp sesion = sesionWhatsappService.crearSesion(telefono);

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

        String telefono = normalizarTelefono(numeroWhatsapp);
        sesionWhatsappService.cerrarSesion(telefono);

        Map<String, Object> response = Map.of(
                "sesionActiva", false,
                "mensaje", "Sesion cerrada correctamente. Escribe 'iniciar sesion' cuando quieras volver."
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private static final int TOKEN_EXPIRACION_MINUTOS = 30;

    @GetMapping("/validar-token")
    @Operation(summary = "Validar token de login OAuth",
            description = "Verifica si un token de login OAuth existe y no ha expirado. Usado por el frontend antes de iniciar el flujo OAuth.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validarToken(
            @RequestParam String token) {

        Optional<WhatsappLoginToken> loginTokenOpt = whatsappLoginTokenRepository.findByToken(token);

        if (loginTokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("El link de login no es valido. Solicita uno nuevo desde WhatsApp."));
        }

        WhatsappLoginToken loginToken = loginTokenOpt.get();

        if (loginToken.isUsado()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Este link de login ya fue utilizado. Solicita uno nuevo desde WhatsApp."));
        }

        if (loginToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("El link de login ha expirado. Solicita uno nuevo desde WhatsApp."));
        }

        Map<String, Object> response = Map.of(
                "valido", true,
                "mensaje", "Token valido. Puedes iniciar sesion."
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/generar-link")
    @Operation(summary = "Generar link de login OAuth para WhatsApp",
            description = "Genera un link temporal para que el usuario inicie sesion con Google/GitHub desde WhatsApp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generarLinkOAuth(
            @RequestParam String numeroWhatsapp) {

        String telefono = normalizarTelefono(numeroWhatsapp);

        String tokenStr = UUID.randomUUID().toString();
        WhatsappLoginToken loginToken = WhatsappLoginToken.builder()
                .id(UUID.randomUUID())
                .token(tokenStr)
                .numeroWhatsapp(telefono)
                .usado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(TOKEN_EXPIRACION_MINUTOS))
                .fechaCreacion(LocalDateTime.now())
                .build();

        whatsappLoginTokenRepository.save(loginToken);
        log.info("Token OAuth generado para telefono: {} (expira en {} min)", telefono, TOKEN_EXPIRACION_MINUTOS);

        String frontendUrl = frontendBaseUrl + "/whatsapp-login?token=" + tokenStr;

        Map<String, Object> response = Map.of(
                "url", frontendUrl,
                "expiraEnMinutos", TOKEN_EXPIRACION_MINUTOS,
                "mensaje", "Abre el siguiente enlace para iniciar sesion con tu cuenta de Google o GitHub."
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/confirmar-oauth")
    @Operation(summary = "Confirmar autenticacion OAuth desde WhatsApp",
            description = "Vincula la cuenta OAuth con el numero de WhatsApp y crea la sesion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmarOAuth(
            @RequestParam String token,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Confirmando OAuth con token: {}...", token.substring(0, Math.min(8, token.length())));

        WhatsappLoginToken loginToken = whatsappLoginTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Token OAuth no encontrado en BD: {}...", token.substring(0, Math.min(8, token.length())));
                    return new DomainException("Token de login invalido o expirado.");
                });

        if (loginToken.isUsado()) {
            log.warn("Token OAuth ya fue usado: {}...", token.substring(0, Math.min(8, token.length())));
            throw new DomainException("Este link de login ya fue utilizado.");
        }

        if (loginToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            log.warn("Token OAuth expirado: {}... (expiraba: {})", token.substring(0, Math.min(8, token.length())), loginToken.getFechaExpiracion());
            throw new DomainException("El link de login ha expirado. Solicita uno nuevo.");
        }

        String jwt = authHeader.replace("Bearer ", "");
        UUID userId = jwtService.extractUserId(jwt);

        if (userId == null) {
            throw new DomainException("Token de autenticacion invalido.");
        }

        String telefono = loginToken.getNumeroWhatsapp();

        Usuario oauthUsuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Usuario no encontrado."));

        // Determinar el usuario final: si ya existe uno con este telefono, vincular OAuth a ese
        Usuario usuario;
        Optional<Usuario> usuarioConTelefono = usuarioRepository.findByTelefono(telefono);

        if (usuarioConTelefono.isPresent() && !usuarioConTelefono.get().getId().equals(oauthUsuario.getId())) {
            // Ya existe un usuario con este telefono: vincular credenciales OAuth a esa cuenta
            usuario = usuarioConTelefono.get();
            if (oauthUsuario.getOauthProvider() != null) {
                usuario.setOauthProvider(oauthUsuario.getOauthProvider());
                usuario.setOauthProviderId(oauthUsuario.getOauthProviderId());
            }
            usuario.setFechaActualizacion(LocalDateTime.now());
            usuarioRepository.save(usuario);

            // Eliminar el usuario OAuth huerfano para evitar duplicados
            try {
                usuarioRepository.deleteById(oauthUsuario.getId());
                log.info("Usuario OAuth huerfano eliminado: {}", oauthUsuario.getEmail());
            } catch (Exception e) {
                log.warn("No se pudo eliminar usuario OAuth huerfano {}: {}", oauthUsuario.getEmail(), e.getMessage());
            }

            userId = usuario.getId();
            log.info("Cuenta OAuth vinculada al usuario existente con telefono: {} ({})", telefono, usuario.getEmail());
        } else if (usuarioConTelefono.isEmpty()) {
            // Ningun usuario tiene este telefono: asignarlo al usuario OAuth
            usuario = oauthUsuario;
            usuario.setTelefono(telefono);
            usuario.setFechaActualizacion(LocalDateTime.now());
            usuarioRepository.save(usuario);
        } else {
            // El mismo usuario ya tiene el telefono asignado
            usuario = oauthUsuario;
        }

        UUID finalUserId = userId;
        Optional<Dispositivo> dispositivoExistente = dispositivoRepository.findByNumeroWhatsapp(telefono);
        if (dispositivoExistente.isEmpty()) {
            Dispositivo dispositivo = Dispositivo.builder()
                    .id(UUID.randomUUID())
                    .usuarioId(finalUserId)
                    .numeroWhatsapp(telefono)
                    .nombreDispositivo("WhatsApp")
                    .tokenDispositivo(UUID.randomUUID().toString())
                    .activo(true)
                    .verificado(true)
                    .fechaCreacion(LocalDateTime.now())
                    .fechaActualizacion(LocalDateTime.now())
                    .ultimaConexion(LocalDateTime.now())
                    .build();
            dispositivoRepository.save(dispositivo);
        } else {
            Dispositivo dispositivo = dispositivoExistente.get();
            dispositivo.setUsuarioId(finalUserId);
            dispositivo.setActivo(true);
            dispositivo.setVerificado(true);
            dispositivo.setUltimaConexion(LocalDateTime.now());
            dispositivo.setFechaActualizacion(LocalDateTime.now());
            dispositivoRepository.save(dispositivo);
        }

        sesionWhatsappService.crearSesion(telefono);

        loginToken.setUsado(true);
        whatsappLoginTokenRepository.save(loginToken);
        log.info("OAuth confirmado exitosamente para telefono: {} (usuario: {})", telefono, usuario.getEmail());

        evolutionApiService.enviarMensaje(telefono,
                "Has iniciado sesion exitosamente en FinanzApp. Ya puedes gestionar tus finanzas desde aqui.");

        Map<String, Object> response = Map.of(
                "sesionActiva", true,
                "mensaje", "Cuenta vinculada exitosamente. Ya puedes usar FinanzApp desde WhatsApp."
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
        return "Tu sesion ha expirado o no esta activa. Solicita un codigo de verificacion para iniciar sesion.";
    }
}
