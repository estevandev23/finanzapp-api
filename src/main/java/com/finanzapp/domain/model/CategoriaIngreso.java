package com.finanzapp.domain.model;

public enum CategoriaIngreso {
    TRABAJO_PRINCIPAL("Trabajo Principal"),
    TRABAJO_EXTRA("Trabajo Extra"),
    GANANCIAS_ADICIONALES("Ganancias Adicionales"),
    INVERSIONES("Inversiones"),
    OTROS("Otros"),
    ABONO("Abono");

    private final String descripcion;

    CategoriaIngreso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
