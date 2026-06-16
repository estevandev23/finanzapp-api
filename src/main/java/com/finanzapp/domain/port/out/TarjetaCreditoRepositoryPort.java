package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.TarjetaCredito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarjetaCreditoRepositoryPort {
    TarjetaCredito save(TarjetaCredito tarjeta);
    Optional<TarjetaCredito> findById(UUID id);
    List<TarjetaCredito> findByUsuarioId(UUID usuarioId);
    void deleteById(UUID id);
}
