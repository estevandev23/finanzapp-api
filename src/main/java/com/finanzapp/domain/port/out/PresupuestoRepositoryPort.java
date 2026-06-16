package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.AlertaPresupuestoEmitida;
import com.finanzapp.domain.model.BolsilloMensual;
import com.finanzapp.domain.model.NivelAlertaPresupuesto;
import com.finanzapp.domain.model.PresupuestoMensual;
import com.finanzapp.domain.model.PresupuestoPlantilla;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PresupuestoRepositoryPort {

    // Plantilla
    PresupuestoPlantilla savePlantilla(PresupuestoPlantilla plantilla);
    Optional<PresupuestoPlantilla> findPlantillaByUsuarioId(UUID usuarioId);
    Optional<PresupuestoPlantilla> findPlantillaById(UUID id);
    void deletePlantillaById(UUID id);
    List<PresupuestoPlantilla> findAllPlantillas();

    // Mensual
    PresupuestoMensual saveMensual(PresupuestoMensual mensual);
    Optional<PresupuestoMensual> findMensualByUsuarioAnioMes(UUID usuarioId, int anio, int mes);
    Optional<PresupuestoMensual> findMensualById(UUID id);
    List<PresupuestoMensual> findMensualesByUsuarioId(UUID usuarioId);
    void deleteMensualById(UUID id);

    // Bolsillo mensual individual (override)
    Optional<BolsilloMensual> findBolsilloMensualById(UUID id);
    BolsilloMensual saveBolsilloMensual(BolsilloMensual bolsillo);

    // Alertas
    boolean existsAlerta(UUID bolsilloMensualId, NivelAlertaPresupuesto nivel);
    AlertaPresupuestoEmitida saveAlerta(AlertaPresupuestoEmitida alerta);
}
