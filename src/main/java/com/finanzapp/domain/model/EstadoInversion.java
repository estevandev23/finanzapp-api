package com.finanzapp.domain.model;

public enum EstadoInversion {
    ACTIVA("Activa"),
    FINALIZADA("Finalizada");

    private final String descripcion;

    EstadoInversion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
