package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Movimiento asignado a un bolsillo dentro del mes: un gasto real ya registrado
 * o una recurrencia de gasto pendiente de confirmar (proyección).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoBolsillo {

    public enum TipoMovimiento { GASTO, RECURRENCIA }

    private TipoMovimiento tipo;
    private UUID id;
    private String descripcion;
    private BigDecimal monto;
    /** Fecha del gasto o próxima fecha de la recurrencia. */
    private LocalDate fecha;
    private String categoriaNombre;
}
