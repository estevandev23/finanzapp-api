package com.finanzapp.application.service;

import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.model.CodigoVerificacion;
import com.finanzapp.domain.model.TipoVerificacion;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.out.CodigoVerificacionRepositoryPort;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import com.finanzapp.infrastructure.service.EmailService;
import com.finanzapp.infrastructure.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificacionService {

    private final CodigoVerificacionRepositoryPort codigoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    private static final int DURACION_MINUTOS = 10;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Genera y envia un codigo 2FA al email del usuario.
     * Retorna el ID de verificacion para usarlo luego en la validacion.
     */
    public UUID enviarCodigo2FA(UUID usuarioId, String email) {
        String codigo = generarCodigo();

        CodigoVerificacion verificacion = CodigoVerificacion.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .codigo(codigo)
                .tipo(TipoVerificacion.DOS_FACTORES)
                .usado(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(DURACION_MINUTOS))
                .build();

        codigoRepository.save(verificacion);
        emailService.enviarCodigoVerificacion(email, codigo, "FinanzApp - Codigo de verificacion");

        return verificacion.getId();
    }

    /**
     * Valida el codigo 2FA. Retorna true si es valido.
     */
    public boolean verificarCodigo2FA(UUID usuarioId, String codigo) {
        var verificacion = codigoRepository
                .findByUsuarioIdAndCodigoAndTipoAndUsadoFalse(usuarioId, codigo, TipoVerificacion.DOS_FACTORES)
                .orElseThrow(() -> new DomainException("Codigo de verificacion invalido"));

        if (verificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new DomainException("El codigo de verificacion ha expirado");
        }

        verificacion.setUsado(true);
        codigoRepository.save(verificacion);
        return true;
    }

    /**
     * Activa o desactiva 2FA para el usuario.
     */
    public void toggle2FA(UUID usuarioId, boolean activar) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));

        usuario.setDosFactoresActivado(activar);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    /**
     * Envia un codigo de verificacion de telefono por SMS.
     */
    public UUID enviarCodigoVerificacionTelefono(UUID usuarioId, String telefono) {
        String codigo = generarCodigo();

        CodigoVerificacion verificacion = CodigoVerificacion.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .codigo(codigo)
                .tipo(TipoVerificacion.VERIFICACION_TELEFONO)
                .usado(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(DURACION_MINUTOS))
                .build();

        codigoRepository.save(verificacion);
        smsService.enviarCodigoVerificacion(telefono, codigo);

        return verificacion.getId();
    }

    /**
     * Verifica el codigo de telefono y marca el telefono como verificado.
     */
    public boolean verificarTelefono(UUID usuarioId, String codigo) {
        var verificacion = codigoRepository
                .findByUsuarioIdAndCodigoAndTipoAndUsadoFalse(
                        usuarioId, codigo, TipoVerificacion.VERIFICACION_TELEFONO)
                .orElseThrow(() -> new DomainException("Codigo de verificacion invalido"));

        if (verificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new DomainException("El codigo de verificacion ha expirado");
        }

        verificacion.setUsado(true);
        codigoRepository.save(verificacion);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        usuario.setTelefonoVerificado(true);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return true;
    }

    /**
     * Genera y envia un codigo de recuperacion de contrasena al email (plantilla HTML).
     */
    public UUID enviarCodigoRecuperacionPassword(UUID usuarioId, String email) {
        String codigo = generarCodigo();

        CodigoVerificacion verificacion = CodigoVerificacion.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .codigo(codigo)
                .tipo(TipoVerificacion.RECUPERACION_PASSWORD)
                .usado(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(DURACION_MINUTOS))
                .build();

        codigoRepository.save(verificacion);
        emailService.enviarRecuperacionPasswordHtml(email, codigo);

        return verificacion.getId();
    }

    /**
     * Genera y envia un codigo de recuperacion de contrasena por SMS.
     */
    public UUID enviarCodigoRecuperacionPasswordSms(UUID usuarioId, String telefono) {
        String codigo = generarCodigo();

        CodigoVerificacion verificacion = CodigoVerificacion.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .codigo(codigo)
                .tipo(TipoVerificacion.RECUPERACION_PASSWORD)
                .usado(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(DURACION_MINUTOS))
                .build();

        codigoRepository.save(verificacion);
        smsService.enviarCodigoVerificacion(telefono, codigo);

        return verificacion.getId();
    }

    /**
     * Valida el codigo de recuperacion sin marcarlo como usado.
     * Usar en el paso intermedio de verificacion (antes de cambiar la contrasena).
     */
    public void validarCodigoRecuperacion(UUID usuarioId, String codigo) {
        var verificacion = codigoRepository
                .findByUsuarioIdAndCodigoAndTipoAndUsadoFalse(usuarioId, codigo, TipoVerificacion.RECUPERACION_PASSWORD)
                .orElseThrow(() -> new DomainException("Codigo de recuperacion invalido"));

        if (verificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new DomainException("El codigo de recuperacion ha expirado");
        }
        // No se marca como usado; se consume en el paso final (reset-password)
    }

    /**
     * Valida el codigo de recuperacion de contrasena y lo marca como usado.
     */
    public void verificarCodigoRecuperacion(UUID usuarioId, String codigo) {
        var verificacion = codigoRepository
                .findByUsuarioIdAndCodigoAndTipoAndUsadoFalse(usuarioId, codigo, TipoVerificacion.RECUPERACION_PASSWORD)
                .orElseThrow(() -> new DomainException("Codigo de recuperacion invalido"));

        if (verificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new DomainException("El codigo de recuperacion ha expirado");
        }

        verificacion.setUsado(true);
        codigoRepository.save(verificacion);
    }

    private String generarCodigo() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
