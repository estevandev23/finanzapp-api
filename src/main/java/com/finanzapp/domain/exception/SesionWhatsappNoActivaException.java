package com.finanzapp.domain.exception;

public class SesionWhatsappNoActivaException extends DomainException {

    public SesionWhatsappNoActivaException() {
        super("No tienes una sesion activa. Escribe 'iniciar sesion' para autenticarte.");
    }

    public SesionWhatsappNoActivaException(String mensaje) {
        super(mensaje);
    }
}
