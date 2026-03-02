package com.finanzapp.application.service;

import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.exception.UsuarioNotFoundException;
import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.in.AuthUseCase;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import com.finanzapp.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements AuthUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final DispositivoRepositoryPort dispositivoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public String login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));

        if (!usuario.isActivo()) {
            throw new DomainException("El usuario está desactivado");
        }

        return jwtService.generateToken(usuario.getEmail(), usuario.getId());
    }

    @Override
    public Usuario authenticateUser(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));

        if (!usuario.isActivo()) {
            throw new DomainException("El usuario está desactivado");
        }

        return usuario;
    }

    @Override
    public String loginWhatsapp(String numeroWhatsapp, String codigoVerificacion) {
        Dispositivo dispositivo = dispositivoRepository.findByNumeroWhatsapp(numeroWhatsapp)
                .orElseThrow(() -> new DomainException("Dispositivo no encontrado"));

        if (!dispositivo.isActivo()) {
            throw new DomainException("El dispositivo está desactivado");
        }

        if (dispositivo.getFechaExpiracionCodigo() != null &&
                dispositivo.getFechaExpiracionCodigo().isBefore(LocalDateTime.now())) {
            throw new DomainException("El código de verificación ha expirado");
        }

        if (!codigoVerificacion.equals(dispositivo.getCodigoVerificacion())) {
            throw new DomainException("Código de verificación incorrecto");
        }

        dispositivo.setVerificado(true);
        dispositivo.setCodigoVerificacion(null);
        dispositivo.setFechaExpiracionCodigo(null);
        dispositivo.setUltimaConexion(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);

        Usuario usuario = usuarioRepository.findById(dispositivo.getUsuarioId())
                .orElseThrow(() -> new UsuarioNotFoundException(dispositivo.getUsuarioId()));

        return jwtService.generateToken(usuario.getEmail(), usuario.getId());
    }

    @Override
    public Usuario loginOAuth(String provider, String providerId, String email, String nombre) {
        // Buscar usuario por proveedor OAuth
        var usuarioExistente = usuarioRepository.findByOAuthProviderAndProviderId(provider, providerId);
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            if (!usuario.isActivo()) {
                throw new DomainException("El usuario está desactivado");
            }
            return usuario;
        }

        // Buscar por email para vincular cuenta existente
        var usuarioPorEmail = usuarioRepository.findByEmail(email);
        if (usuarioPorEmail.isPresent()) {
            Usuario usuario = usuarioPorEmail.get();
            usuario.setOauthProvider(provider);
            usuario.setOauthProviderId(providerId);
            usuario.setFechaActualizacion(LocalDateTime.now());
            return usuarioRepository.save(usuario);
        }

        // Crear usuario nuevo con OAuth
        Usuario nuevoUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre(nombre != null ? nombre : email.split("@")[0])
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .oauthProvider(provider)
                .oauthProviderId(providerId)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        return usuarioRepository.save(nuevoUsuario);
    }

    @Override
    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new DomainException("Ya existe un usuario con este email");
        }

        if (usuario.getTelefono() != null && usuarioRepository.existsByTelefono(usuario.getTelefono())) {
            throw new DomainException("Ya existe un usuario con este número de teléfono");
        }

        usuario.setId(UUID.randomUUID());
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    @Override
    public void cambiarPassword(UUID usuarioId, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Override
    public void logout(String token) {
        // El logout se maneja en el cliente eliminando el token
    }

    @Override
    public String refreshToken(String token) {
        String email = jwtService.extractUsername(token);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));

        if (!usuario.isActivo()) {
            throw new DomainException("El usuario está desactivado");
        }

        return jwtService.generateToken(usuario.getEmail(), usuario.getId());
    }
}
