package com.finanzapp.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recurrencia {
    private UUID id;
    private UUID usuarioId;
    private TipoRecurrencia tipo;
    private FrecuenciaRecurrencia frecuencia;
    private String descripcion;
    private BigDecimal monto;
    private CategoriaIngreso categoriaIngreso;
    private CategoriaGasto categoriaGasto;
    private UUID categoriaPersonalizadaId;
    private String categoriaNombre;
    private String categoriaColor;
    private MetodoPago metodoPago;
    private UUID tarjetaId;
    private String tarjetaNombre;
    private UUID bolsilloId;
    private String bolsilloNombre;
    private int diaVencimiento;
    private Integer mesReferencia;
    private LocalDate proximaFecha;
    private LocalDate ultimaConfirmacionFecha;
    private boolean activa;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public boolean isVencida(LocalDate hoy) {
        return activa && proximaFecha != null && !proximaFecha.isAfter(hoy);
    }

    public long diasParaVencer(LocalDate hoy) {
        if (proximaFecha == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.DAYS.between(hoy, proximaFecha);
    }

    public void avanzarProximaFecha() {
        if (frecuencia == null || proximaFecha == null) return;
        this.proximaFecha = frecuencia.calcularSiguiente(proximaFecha, diaVencimiento);
    }
}
