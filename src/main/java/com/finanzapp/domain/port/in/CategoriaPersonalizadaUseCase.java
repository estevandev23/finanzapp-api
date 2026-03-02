package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.CategoriaPersonalizada;
import com.finanzapp.domain.model.TipoCategoria;

import java.util.List;
import java.util.UUID;

public interface CategoriaPersonalizadaUseCase {
    CategoriaPersonalizada crear(CategoriaPersonalizada categoria);
    CategoriaPersonalizada obtenerPorId(UUID id);
    List<CategoriaPersonalizada> listarPorUsuario(UUID usuarioId);
    List<CategoriaPersonalizada> listarPorUsuarioYTipo(UUID usuarioId, TipoCategoria tipo);
    CategoriaPersonalizada actualizar(UUID id, CategoriaPersonalizada categoria);
    void eliminar(UUID id);
}
