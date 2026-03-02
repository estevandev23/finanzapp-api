package com.finanzapp.domain.model;

public enum EstadoDeuda {
    PENDIENTE("Pendiente"),
    EN_CURSO("En curso"),
    COMPLETADA("Completada");

    private final String descripcion;

    EstadoDeuda(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
