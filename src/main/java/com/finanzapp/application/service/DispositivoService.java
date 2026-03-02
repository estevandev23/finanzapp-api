package com.finanzapp.application.service;

import com.finanzapp.domain.exception.DispositivoNoVerificadoException;
import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.port.in.DispositivoUseCase;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DispositivoService implements DispositivoUseCase {

    private static final int CODIGO_LONGITUD = 6;
    private static final int CODIGO_EXPIRACION_MINUTOS = 10;

    private final DispositivoRepositoryPort dispositivoRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public Dispositivo registrar(UUID usuarioId, String numeroWhatsapp, String nombreDispositivo) {
        if (dispositivoRepository.existsByNumeroWhatsappAndUsuarioId(numeroWhatsapp, usuarioId)) {
            throw new DomainException("Ya existe un dispositivo registrado con este número de WhatsApp");
        }

        String codigo = generarCodigoVerificacion();

        Dispositivo dispositivo = Dispositivo.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .numeroWhatsapp(numeroWhatsapp)
                .nombreDispositivo(nombreDispositivo)
                .tokenDispositivo(UUID.randomUUID().toString())
                .activo(true)
                .verificado(false)
                .codigoVerificacion(codigo)
                .fechaExpiracionCodigo(LocalDateTime.now().plusMinutes(CODIGO_EXPIRACION_MINUTOS))
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        return dispositivoRepository.save(dispositivo);
    }

    @Override
    public Dispositivo verificar(String numeroWhatsapp, String codigoVerificacion) {
        Dispositivo dispositivo = dispositivoRepository.findByNumeroWhatsapp(numeroWhatsapp)
                .orElseThrow(() -> new DomainException("Dispositivo no encontrado"));

        if (dispositivo.getFechaExpiracionCodigo().isBefore(LocalDateTime.now())) {
            throw new DomainException("El código de verificación ha expirado");
        }

        if (!dispositivo.getCodigoVerificacion().equals(codigoVerificacion)) {
            throw new DomainException("Código de verificación incorrecto");
        }

        dispositivo.setVerificado(true);
        dispositivo.setCodigoVerificacion(null);
        dispositivo.setFechaExpiracionCodigo(null);
        dispositivo.setUltimaConexion(LocalDateTime.now());
        dispositivo.setFechaActualizacion(LocalDateTime.now());

        return dispositivoRepository.save(dispositivo);
    }

    @Override
    @Transactional(readOnly = true)
    public Dispositivo obtenerPorId(UUID id) {
        return dispositivoRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Dispositivo", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Dispositivo obtenerPorNumeroWhatsapp(String numeroWhatsapp) {
        Dispositivo dispositivo = dispositivoRepository.findByNumeroWhatsapp(numeroWhatsapp)
                .orElseThrow(() -> new DomainException("Dispositivo no encontrado"));

        if (!dispositivo.isVerificado()) {
            throw new DispositivoNoVerificadoException(numeroWhatsapp);
        }

        return dispositivo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispositivo> listarPorUsuario(UUID usuarioId) {
        return dispositivoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public void desactivar(UUID dispositivoId) {
        Dispositivo dispositivo = obtenerPorId(dispositivoId);
        dispositivo.setActivo(false);
        dispositivo.setFechaActualizacion(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);
    }

    @Override
    public void eliminar(UUID dispositivoId) {
        obtenerPorId(dispositivoId);
        dispositivoRepository.deleteById(dispositivoId);
    }

    @Override
    public String generarNuevoCodigo(UUID dispositivoId) {
        Dispositivo dispositivo = obtenerPorId(dispositivoId);

        String codigo = generarCodigoVerificacion();
        dispositivo.setCodigoVerificacion(codigo);
        dispositivo.setFechaExpiracionCodigo(LocalDateTime.now().plusMinutes(CODIGO_EXPIRACION_MINUTOS));
        dispositivo.setFechaActualizacion(LocalDateTime.now());

        dispositivoRepository.save(dispositivo);
        return codigo;
    }

    private String generarCodigoVerificacion() {
        int codigo = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(codigo);
    }
}
