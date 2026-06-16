package com.finanzapp.domain.model;

public enum TipoRecurrencia {
    INGRESO("Ingreso"),
    GASTO("Gasto");

    private final String descripcion;

    TipoRecurrencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
