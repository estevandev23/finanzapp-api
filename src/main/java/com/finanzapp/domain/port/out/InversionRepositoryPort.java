package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.EstadoInversion;
import com.finanzapp.domain.model.Inversion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InversionRepositoryPort {
    Inversion save(Inversion inversion);
    Optional<Inversion> findById(UUID id);
    List<Inversion> findByUsuarioId(UUID usuarioId);
    List<Inversion> findByUsuarioIdAndEstado(UUID usuarioId, EstadoInversion estado);
    void deleteById(UUID id);
}
