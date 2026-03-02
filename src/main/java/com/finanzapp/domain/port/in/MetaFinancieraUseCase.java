package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.EstadoMeta;
import com.finanzapp.domain.model.MetaFinanciera;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MetaFinancieraUseCase {
    MetaFinanciera crear(MetaFinanciera meta);
    MetaFinanciera obtenerPorId(UUID id);
    List<MetaFinanciera> listarPorUsuario(UUID usuarioId);
    List<MetaFinanciera> listarPorEstado(UUID usuarioId, EstadoMeta estado);
    MetaFinanciera actualizar(UUID id, MetaFinanciera meta);
    MetaFinanciera registrarProgreso(UUID metaId, UUID usuarioId, BigDecimal monto, String descripcion);
    MetaFinanciera cambiarEstado(UUID metaId, EstadoMeta nuevoEstado);
    void eliminar(UUID id);
}
