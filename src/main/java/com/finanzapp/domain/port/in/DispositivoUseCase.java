package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.Dispositivo;

import java.util.List;
import java.util.UUID;

public interface DispositivoUseCase {
    Dispositivo registrar(UUID usuarioId, String numeroWhatsapp, String nombreDispositivo);
    Dispositivo verificar(String numeroWhatsapp, String codigoVerificacion);
    Dispositivo obtenerPorId(UUID id);
    Dispositivo obtenerPorNumeroWhatsapp(String numeroWhatsapp);
    List<Dispositivo> listarPorUsuario(UUID usuarioId);
    void desactivar(UUID dispositivoId);
    void eliminar(UUID dispositivoId);
    String generarNuevoCodigo(UUID dispositivoId);
}
