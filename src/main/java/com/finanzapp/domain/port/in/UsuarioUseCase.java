package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.Usuario;

import java.util.UUID;

public interface UsuarioUseCase {
    Usuario registrar(Usuario usuario);
    Usuario obtenerPorId(UUID id);
    Usuario obtenerPorEmail(String email);
    Usuario actualizar(UUID id, Usuario usuario);
    void eliminar(UUID id);
    void cambiarPassword(UUID id, String passwordActual, String nuevaPassword);
}
