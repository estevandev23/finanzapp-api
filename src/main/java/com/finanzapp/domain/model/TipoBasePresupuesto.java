package com.finanzapp.domain.model;

public enum TipoBasePresupuesto {
    INGRESOS_MES("Ingresos del mes"),
    MONTO_FIJO("Monto fijo"),
    INGRESOS_RECURRENTES("Ingresos recurrentes");

    private final String descripcion;

    TipoBasePresupuesto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
