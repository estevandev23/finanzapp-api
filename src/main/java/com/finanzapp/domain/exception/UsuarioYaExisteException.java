package com.finanzapp.domain.exception;

public class UsuarioYaExisteException extends DomainException {

    public UsuarioYaExisteException(String email) {
        super("Ya existe un usuario registrado con el email: " + email);
    }
}
