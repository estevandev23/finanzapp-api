package com.finanzapp.domain.model;

import java.time.LocalDate;

public enum FrecuenciaRecurrencia {
    MENSUAL("Mensual"),
    QUINCENAL("Quincenal"),
    SEMESTRAL("Semestral"),
    ANUAL("Anual");

    private final String descripcion;

    FrecuenciaRecurrencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Calcula la próxima fecha de vencimiento a partir de la fecha base.
     * Para frecuencias por meses ajusta el día al último día disponible cuando
     * diaObjetivo no existe en el mes destino (ej. 31 -> 28/29 en febrero).
     * Para QUINCENAL ignora diaObjetivo y suma 15 días corridos.
     */
    public LocalDate calcularSiguiente(LocalDate base, int diaObjetivo) {
        return switch (this) {
            case MENSUAL -> ajustarDia(base.plusMonths(1), diaObjetivo);
            case QUINCENAL -> base.plusDays(15);
            case SEMESTRAL -> ajustarDia(base.plusMonths(6), diaObjetivo);
            case ANUAL -> ajustarDia(base.plusYears(1), diaObjetivo);
        };
    }

    private LocalDate ajustarDia(LocalDate fecha, int diaObjetivo) {
        int diaSeguro = Math.min(diaObjetivo, fecha.lengthOfMonth());
        return fecha.withDayOfMonth(diaSeguro);
    }
}
