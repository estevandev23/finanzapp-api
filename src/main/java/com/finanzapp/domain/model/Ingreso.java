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
public class Ingreso {
    private UUID id;
    private UUID usuarioId;
    private BigDecimal monto;
    private CategoriaIngreso categoria;
    private String descripcion;
    private LocalDate fecha;
    private BigDecimal montoAhorro;
    private UUID metaId;
    private UUID prestamoId;
    private UUID categoriaPersonalizadaId;
    private String categoriaNombre;
    private String categoriaColor;
    private MetodoPago metodoPago;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public BigDecimal getMontoDisponible() {
        if (montoAhorro == null) {
            return monto;
        }
        return monto.subtract(montoAhorro);
    }
}
