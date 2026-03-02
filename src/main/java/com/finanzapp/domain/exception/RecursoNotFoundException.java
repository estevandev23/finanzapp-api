package com.finanzapp.domain.exception;

import java.util.UUID;

public class RecursoNotFoundException extends DomainException {

    public RecursoNotFoundException(String recurso, UUID id) {
        super(recurso + " no encontrado con ID: " + id);
    }
}
