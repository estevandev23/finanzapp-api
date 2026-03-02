package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.EstadoInversion;
import com.finanzapp.domain.model.Inversion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InversionUseCase {
    Inversion crear(Inversion inversion);
    Inversion obtenerPorId(UUID id);
    List<Inversion> listarPorUsuario(UUID usuarioId);
    List<Inversion> listarPorEstado(UUID usuarioId, EstadoInversion estado);
    Inversion registrarRetorno(UUID inversionId, BigDecimal retornoReal, LocalDate fechaRetorno);
    void eliminar(UUID id);
}
