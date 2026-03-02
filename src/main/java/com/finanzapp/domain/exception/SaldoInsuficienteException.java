package com.finanzapp.domain.exception;

public class SaldoInsuficienteException extends DomainException {

    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
