package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.CodigoVerificacion;
import com.finanzapp.domain.model.TipoVerificacion;

import java.util.Optional;
import java.util.UUID;

public interface CodigoVerificacionRepositoryPort {
    CodigoVerificacion save(CodigoVerificacion codigo);
    Optional<CodigoVerificacion> findById(UUID id);
    Optional<CodigoVerificacion> findByUsuarioIdAndCodigoAndTipoAndUsadoFalse(
            UUID usuarioId, String codigo, TipoVerificacion tipo);
    void deleteExpiredCodes();
}
