package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Resultado computado (no persistido) que combina un bolsillo del mes con su uso:
 * gastos reales del mes + recurrencias de gasto pendientes de confirmar (proyección).
 * Una recurrencia ya confirmada en el mes no se proyecta porque su gasto generado
 * ya cuenta en montoGastado (evita sumar doble el mismo movimiento).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoBolsilloMensual {
    private BolsilloMensual bolsillo;
    private BigDecimal montoGastado;
    private BigDecimal montoRecurrente;
    private BigDecimal montoComprometido;
    private BigDecimal porcentajeUso;
    private NivelAlertaPresupuesto nivel;
    private List<MovimientoBolsillo> movimientos;

    public static EstadoBolsilloMensual calcular(BolsilloMensual bolsillo, BigDecimal montoGastado) {
        return calcular(bolsillo, montoGastado, BigDecimal.ZERO, List.of());
    }

    public static EstadoBolsilloMensual calcular(BolsilloMensual bolsillo, BigDecimal montoGastado,
                                                 BigDecimal montoRecurrente,
                                                 List<MovimientoBolsillo> movimientos) {
        BigDecimal limite = bolsillo.getMontoLimite();
        BigDecimal gastado = montoGastado != null ? montoGastado : BigDecimal.ZERO;
        BigDecimal recurrente = montoRecurrente != null ? montoRecurrente : BigDecimal.ZERO;
        BigDecimal comprometido = gastado.add(recurrente);

        BigDecimal porcentajeUso = BigDecimal.ZERO;
        if (limite != null && limite.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeUso = comprometido.multiply(BigDecimal.valueOf(100))
                    .divide(limite, 2, RoundingMode.HALF_UP);
        }

        NivelAlertaPresupuesto nivel;
        if (porcentajeUso.compareTo(BigDecimal.valueOf(100)) >= 0) {
            nivel = NivelAlertaPresupuesto.EXCEDIDO;
        } else if (porcentajeUso.compareTo(BigDecimal.valueOf(80)) >= 0) {
            nivel = NivelAlertaPresupuesto.ADVERTENCIA;
        } else {
            nivel = NivelAlertaPresupuesto.OK;
        }

        return EstadoBolsilloMensual.builder()
                .bolsillo(bolsillo)
                .montoGastado(gastado)
                .montoRecurrente(recurrente)
                .montoComprometido(comprometido)
                .porcentajeUso(porcentajeUso)
                .nivel(nivel)
                .movimientos(movimientos != null ? movimientos : List.of())
                .build();
    }
}
