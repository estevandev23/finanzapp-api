package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.Dispositivo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DispositivoRepositoryPort {
    Dispositivo save(Dispositivo dispositivo);
    Optional<Dispositivo> findById(UUID id);
    Optional<Dispositivo> findByNumeroWhatsapp(String numeroWhatsapp);
    Optional<Dispositivo> findByTokenDispositivo(String token);
    List<Dispositivo> findByUsuarioId(UUID usuarioId);
    List<Dispositivo> findByUsuarioIdAndActivo(UUID usuarioId, boolean activo);
    boolean existsByNumeroWhatsappAndUsuarioId(String numeroWhatsapp, UUID usuarioId);
    void deleteById(UUID id);
}
