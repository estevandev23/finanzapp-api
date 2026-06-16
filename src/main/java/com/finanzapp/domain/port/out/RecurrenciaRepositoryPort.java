package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoRecurrencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurrenciaRepositoryPort {
    Recurrencia save(Recurrencia recurrencia);
    Optional<Recurrencia> findById(UUID id);
    List<Recurrencia> findByUsuarioId(UUID usuarioId);
    List<Recurrencia> findByUsuarioIdAndTipo(UUID usuarioId, TipoRecurrencia tipo);
    List<Recurrencia> findActivasByUsuarioId(UUID usuarioId);
    List<Recurrencia> findActivasEnRangoProximaFecha(LocalDate desde, LocalDate hasta);
    void deleteById(UUID id);
}
