package com.finanzapp.domain.model;

public enum EstadoTarjeta {
    ACTIVA("Activa"),
    BLOQUEADA("Bloqueada"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoTarjeta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
