package com.finanzapp.domain.exception;

public class DispositivoNoVerificadoException extends DomainException {

    public DispositivoNoVerificadoException(String numeroWhatsapp) {
        super("El dispositivo con número " + numeroWhatsapp + " no está verificado");
    }
}
