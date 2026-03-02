package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inversion {
    private UUID id;
    private UUID usuarioId;
    private UUID gastoId;
    private UUID ingresoId;
    private String nombre;
    private String descripcion;
    private BigDecimal monto;
    private BigDecimal retornoEsperado;
    private BigDecimal retornoReal;
    private EstadoInversion estado;
    private LocalDate fechaInversion;
    private LocalDate fechaRetorno;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public BigDecimal calcularGanancia() {
        if (retornoReal == null) {
            return null;
        }
        return retornoReal.subtract(monto);
    }
}
