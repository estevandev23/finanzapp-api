package com.finanzapp.domain.model;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    NEQUI("Nequi"),
    BANCOLOMBIA("Bancolombia"),
    TARJETA_CREDITO("Tarjeta de Crédito"),
    OTRO("Otro");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
