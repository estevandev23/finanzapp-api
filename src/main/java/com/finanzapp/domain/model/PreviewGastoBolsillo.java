package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado computado (no persistido) que proyecta el impacto de un nuevo gasto sobre un
 * bolsillo del presupuesto del mes. Permite mostrar al usuario, antes de confirmar, qué
 * porcentaje del bolsillo lleva consumido y en cuánto quedaría con el gasto a registrar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewGastoBolsillo {
    private UUID bolsilloOrigenId;
    private String nombre;
    private String color;
    private BigDecimal montoLimite;
    private BigDecimal montoNuevoGasto;
    private BigDecimal montoGastadoActual;
    private BigDecimal porcentajeUsoActual;
    private BigDecimal montoProyectado;
    private BigDecimal porcentajeUsoProyectado;
    private BigDecimal montoRestanteProyectado;
    private NivelAlertaPresupuesto nivelActual;
    private NivelAlertaPresupuesto nivelProyectado;
    private boolean excedeProyectado;
}
