package com.finanzapp.domain.exception;

import java.util.UUID;

public class UsuarioNotFoundException extends DomainException {

    public UsuarioNotFoundException(UUID id) {
        super("Usuario no encontrado con ID: " + id);
    }

    public UsuarioNotFoundException(String email) {
        super("Usuario no encontrado con email: " + email);
    }
}
