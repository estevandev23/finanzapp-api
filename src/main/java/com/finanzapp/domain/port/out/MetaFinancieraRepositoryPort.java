package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.EstadoMeta;
import com.finanzapp.domain.model.MetaFinanciera;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetaFinancieraRepositoryPort {
    MetaFinanciera save(MetaFinanciera meta);
    Optional<MetaFinanciera> findById(UUID id);
    List<MetaFinanciera> findByUsuarioId(UUID usuarioId);
    List<MetaFinanciera> findByUsuarioIdAndEstado(UUID usuarioId, EstadoMeta estado);
    void deleteById(UUID id);
}
