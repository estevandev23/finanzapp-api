package com.finanzapp.domain.model;

public enum EstadoMeta {
    ACTIVA("Activa"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    PAUSADA("Pausada");

    private final String descripcion;

    EstadoMeta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
