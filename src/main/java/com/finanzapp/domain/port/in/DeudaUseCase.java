package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.AbonoDeuda;
import com.finanzapp.domain.model.Deuda;
import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.TipoDeuda;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DeudaUseCase {
    Deuda registrar(Deuda deuda);
    Deuda obtenerPorId(UUID id);
    List<Deuda> listarPorUsuario(UUID usuarioId);
    List<Deuda> listarPorTipo(UUID usuarioId, TipoDeuda tipo);
    List<Deuda> listarPorEstado(UUID usuarioId, EstadoDeuda estado);
    Deuda actualizar(UUID id, Deuda deuda);
    void eliminar(UUID id);
    AbonoDeuda registrarAbono(UUID deudaId, BigDecimal monto, String descripcion);
    List<AbonoDeuda> listarAbonos(UUID deudaId);
    BigDecimal obtenerTotalDeudas(UUID usuarioId);
    BigDecimal obtenerTotalPrestamos(UUID usuarioId);
    BigDecimal obtenerTotalAbonosPrestamosRecibidos(UUID usuarioId);
}
