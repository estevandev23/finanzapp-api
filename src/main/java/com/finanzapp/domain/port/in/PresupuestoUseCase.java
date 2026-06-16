package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.BolsilloMensual;
import com.finanzapp.domain.model.EstadoPresupuestoMensual;
import com.finanzapp.domain.model.PresupuestoMensual;
import com.finanzapp.domain.model.PresupuestoPlantilla;
import com.finanzapp.domain.model.PreviewGastoBolsillo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PresupuestoUseCase {

    /** Crea o reemplaza completamente la plantilla del usuario (junto con sus bolsillos). */
    PresupuestoPlantilla guardarPlantilla(UUID usuarioId, PresupuestoPlantilla plantilla);

    Optional<PresupuestoPlantilla> obtenerPlantilla(UUID usuarioId);

    /** Devuelve (o genera la primera vez) el snapshot del mes y lo combina con los gastos reales. */
    EstadoPresupuestoMensual obtenerEstadoMensual(UUID usuarioId, int anio, int mes);

    /** Fuerza la regeneración del snapshot mensual usando la plantilla actual. */
    PresupuestoMensual regenerarMensual(UUID usuarioId, int anio, int mes);

    /** Override puntual: cambia el monto/porcentaje de un bolsillo dentro de un mes ya generado. */
    BolsilloMensual actualizarBolsilloMensual(UUID usuarioId, UUID bolsilloMensualId,
                                              BigDecimal nuevoPorcentaje, BigDecimal nuevoMontoLimite);

    /** Listado de presupuestos mensuales históricos del usuario. */
    List<PresupuestoMensual> listarMensuales(UUID usuarioId);

    /**
     * Proyecta el impacto de un nuevo gasto sobre un bolsillo del mes indicado, devolviendo
     * el porcentaje ya consumido y el que quedaría tras registrar el gasto.
     */
    PreviewGastoBolsillo previsualizarGasto(UUID usuarioId, UUID bolsilloOrigenId,
                                            BigDecimal monto, int anio, int mes);
}
