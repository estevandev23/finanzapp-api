package com.finanzapp.domain.model;

public enum CategoriaGasto {
    COMIDA("Comida"),
    PAREJA("Pareja"),
    COMPRAS("Compras"),
    TRANSPORTE("Transporte"),
    SERVICIOS("Servicios"),
    ENTRETENIMIENTO("Entretenimiento"),
    SALUD("Salud"),
    EDUCACION("Educación"),
    INVERSIONES("Inversiones"),
    OTROS("Otros"),
    ABONO("Abono");

    private final String descripcion;

    CategoriaGasto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
