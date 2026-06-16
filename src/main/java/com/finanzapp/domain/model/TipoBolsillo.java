package com.finanzapp.domain.model;

public enum TipoBolsillo {
    GASTO("Gasto"),
    AHORRO_OBLIGATORIO("Ahorro obligatorio"),
    AHORRO_EMERGENCIA("Ahorro de emergencia"),
    OTRO("Otro");

    private final String descripcion;

    TipoBolsillo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
