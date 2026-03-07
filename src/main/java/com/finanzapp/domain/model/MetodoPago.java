package com.finanzapp.domain.model;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    NEQUI("Nequi"),
    BANCOLOMBIA("Bancolombia"),
    OTRO("Otro");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
