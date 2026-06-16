package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.AlertaPresupuestoEmitida;
import com.finanzapp.domain.model.BolsilloMensual;
import com.finanzapp.domain.model.NivelAlertaPresupuesto;
import com.finanzapp.domain.model.PresupuestoMensual;
import com.finanzapp.domain.model.PresupuestoPlantilla;
import com.finanzapp.domain.port.out.PresupuestoRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.BolsilloMensualEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.PresupuestoMensualEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.PresupuestoPlantillaEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.PresupuestoMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.AlertaPresupuestoJpaRepository;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.BolsilloMensualJpaRepository;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.PresupuestoMensualJpaRepository;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.PresupuestoPlantillaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PresupuestoRepositoryAdapter implements PresupuestoRepositoryPort {

    private final PresupuestoPlantillaJpaRepository plantillaRepo;
    private final PresupuestoMensualJpaRepository mensualRepo;
    private final BolsilloMensualJpaRepository bolsilloMensualRepo;
    private final AlertaPresupuestoJpaRepository alertaRepo;
    private final PresupuestoMapper mapper;

    @Override
    public PresupuestoPlantilla savePlantilla(PresupuestoPlantilla plantilla) {
        PresupuestoPlantillaEntity entity = mapper.toEntity(plantilla);
        return mapper.toDomain(plantillaRepo.save(entity));
    }

    @Override
    public Optional<PresupuestoPlantilla> findPlantillaByUsuarioId(UUID usuarioId) {
        return plantillaRepo.findByUsuarioId(usuarioId).map(mapper::toDomain);
    }

    @Override
    public Optional<PresupuestoPlantilla> findPlantillaById(UUID id) {
        return plantillaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deletePlantillaById(UUID id) {
        plantillaRepo.deleteById(id);
    }

    @Override
    public List<PresupuestoPlantilla> findAllPlantillas() {
        return plantillaRepo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public PresupuestoMensual saveMensual(PresupuestoMensual mensual) {
        PresupuestoMensualEntity entity = mapper.toEntity(mensual);
        return mapper.toDomain(mensualRepo.save(entity));
    }

    @Override
    public Optional<PresupuestoMensual> findMensualByUsuarioAnioMes(UUID usuarioId, int anio, int mes) {
        return mensualRepo.findByUsuarioIdAndAnioAndMes(usuarioId, anio, mes).map(mapper::toDomain);
    }

    @Override
    public Optional<PresupuestoMensual> findMensualById(UUID id) {
        return mensualRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PresupuestoMensual> findMensualesByUsuarioId(UUID usuarioId) {
        return mensualRepo.findByUsuarioIdOrderByAnioDescMesDesc(usuarioId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public void deleteMensualById(UUID id) {
        mensualRepo.deleteById(id);
    }

    @Override
    public Optional<BolsilloMensual> findBolsilloMensualById(UUID id) {
        return bolsilloMensualRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public BolsilloMensual saveBolsilloMensual(BolsilloMensual bolsillo) {
        BolsilloMensualEntity entity = bolsilloMensualRepo.findById(bolsillo.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "BolsilloMensual no encontrado: " + bolsillo.getId()));
        entity.setNombre(bolsillo.getNombre());
        entity.setTipo(bolsillo.getTipo());
        entity.setPorcentaje(bolsillo.getPorcentaje());
        entity.setMontoLimite(bolsillo.getMontoLimite());
        entity.setColor(bolsillo.getColor());
        if (bolsillo.getOrden() != null) entity.setOrden(bolsillo.getOrden());
        return mapper.toDomain(bolsilloMensualRepo.save(entity));
    }

    @Override
    public boolean existsAlerta(UUID bolsilloMensualId, NivelAlertaPresupuesto nivel) {
        return alertaRepo.existsByBolsilloMensualIdAndNivel(bolsilloMensualId, nivel);
    }

    @Override
    public AlertaPresupuestoEmitida saveAlerta(AlertaPresupuestoEmitida alerta) {
        return mapper.toDomain(alertaRepo.save(mapper.toEntity(alerta)));
    }
}
