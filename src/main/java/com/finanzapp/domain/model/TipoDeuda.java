package com.finanzapp.domain.model;

public enum TipoDeuda {
    DEUDA("Deuda - Dinero que debo"),
    PRESTAMO("Prestamo - Dinero que me deben");

    private final String descripcion;

    TipoDeuda(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
