package com.finanzapp.application.service;

import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.model.SesionWhatsapp;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.in.SesionWhatsappUseCase;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.domain.port.out.SesionWhatsappRepositoryPort;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import com.finanzapp.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SesionWhatsappService implements SesionWhatsappUseCase {

    private final SesionWhatsappRepositoryPort sesionRepository;
    private final DispositivoRepositoryPort dispositivoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public Optional<SesionWhatsapp> verificarSesion(String numeroWhatsapp) {
        Optional<SesionWhatsapp> sesionOpt = sesionRepository.findByNumeroWhatsappAndActivaTrue(numeroWhatsapp);

        if (sesionOpt.isEmpty()) {
            return Optional.empty();
        }

        SesionWhatsapp sesion = sesionOpt.get();

        if (sesion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            log.info("Sesion expirada para el numero: {}", numeroWhatsapp);
            return Optional.empty();
        }

        return Optional.of(sesion);
    }

    @Override
    public SesionWhatsapp crearSesion(String numeroWhatsapp) {
        Dispositivo dispositivo = dispositivoRepository.findByNumeroWhatsapp(numeroWhatsapp)
                .orElseThrow(() -> new DomainException("No se encontro un dispositivo registrado para este numero de WhatsApp."));

        if (!dispositivo.isVerificado()) {
            throw new DomainException("El dispositivo no esta verificado. Completa la verificacion primero.");
        }

        if (!dispositivo.isActivo()) {
            throw new DomainException("El dispositivo esta desactivado.");
        }

        Usuario usuario = usuarioRepository.findById(dispositivo.getUsuarioId())
                .orElseThrow(() -> new DomainException("No se encontro el usuario asociado a este dispositivo."));

        // Invalidar sesion previa si existe
        sesionRepository.findByNumeroWhatsapp(numeroWhatsapp)
                .ifPresent(sesionPrevia -> sesionRepository.deleteByNumeroWhatsapp(numeroWhatsapp));

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getId());
        String refreshToken = jwtService.generateRefreshToken(usuario.getEmail(), usuario.getId());

        long expiracionMs = jwtService.getJwtExpiration();
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusSeconds(expiracionMs / 1000);

        SesionWhatsapp sesion = SesionWhatsapp.builder()
                .id(UUID.randomUUID())
                .numeroWhatsapp(numeroWhatsapp)
                .usuarioId(usuario.getId())
                .token(token)
                .refreshToken(refreshToken)
                .activa(true)
                .fechaExpiracion(fechaExpiracion)
                .ultimaActividad(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        log.info("Sesion creada para el numero: {} (usuario: {})", numeroWhatsapp, usuario.getEmail());
        return sesionRepository.save(sesion);
    }

    @Override
    public SesionWhatsapp renovarSesion(String numeroWhatsapp) {
        SesionWhatsapp sesion = sesionRepository.findByNumeroWhatsapp(numeroWhatsapp)
                .orElseThrow(() -> new DomainException("No existe una sesion para este numero. Inicia sesion primero."));

        Dispositivo dispositivo = dispositivoRepository.findByNumeroWhatsapp(numeroWhatsapp)
                .orElseThrow(() -> new DomainException("Dispositivo no encontrado."));

        Usuario usuario = usuarioRepository.findById(dispositivo.getUsuarioId())
                .orElseThrow(() -> new DomainException("Usuario no encontrado."));

        String nuevoToken = jwtService.generateToken(usuario.getEmail(), usuario.getId());
        String nuevoRefreshToken = jwtService.generateRefreshToken(usuario.getEmail(), usuario.getId());

        long expiracionMs = jwtService.getJwtExpiration();
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusSeconds(expiracionMs / 1000);

        sesion.setToken(nuevoToken);
        sesion.setRefreshToken(nuevoRefreshToken);
        sesion.setActiva(true);
        sesion.setFechaExpiracion(fechaExpiracion);
        sesion.setUltimaActividad(LocalDateTime.now());
        sesion.setFechaActualizacion(LocalDateTime.now());

        log.info("Sesion renovada para el numero: {}", numeroWhatsapp);
        return sesionRepository.save(sesion);
    }

    @Override
    public void cerrarSesion(String numeroWhatsapp) {
        sesionRepository.findByNumeroWhatsapp(numeroWhatsapp)
                .ifPresent(sesion -> {
                    sesion.setActiva(false);
                    sesion.setFechaActualizacion(LocalDateTime.now());
                    sesionRepository.save(sesion);
                    log.info("Sesion cerrada para el numero: {}", numeroWhatsapp);
                });
    }

    /**
     * Actualiza la ultima actividad de la sesion.
     * Se invoca desde los endpoints WhatsApp tras cada peticion exitosa.
     */
    public void actualizarActividad(String numeroWhatsapp) {
        sesionRepository.findByNumeroWhatsappAndActivaTrue(numeroWhatsapp)
                .ifPresent(sesion -> {
                    sesion.setUltimaActividad(LocalDateTime.now());
                    sesion.setFechaActualizacion(LocalDateTime.now());
                    sesionRepository.save(sesion);
                });
    }
}
