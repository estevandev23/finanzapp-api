package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.TarjetaCredito;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TarjetaCreditoUseCase {

    TarjetaCredito crear(TarjetaCredito tarjeta);

    TarjetaCredito obtenerPorId(UUID id);

    List<TarjetaCredito> listarPorUsuario(UUID usuarioId);

    TarjetaCredito actualizar(UUID id, TarjetaCredito datos);

    void eliminar(UUID id);

    /** Aumenta el cupo usado cuando se registra un gasto con tarjeta. */
    void aumentarCupoUsado(UUID tarjetaId, BigDecimal monto);

    /** Disminuye el cupo usado cuando se elimina o reduce un gasto con tarjeta. */
    void disminuirCupoUsado(UUID tarjetaId, BigDecimal monto);
}
