package com.finanzapp.infrastructure.adapter.out.persistence.mapper;

import com.finanzapp.domain.model.AlertaPresupuestoEmitida;
import com.finanzapp.domain.model.Bolsillo;
import com.finanzapp.domain.model.BolsilloMensual;
import com.finanzapp.domain.model.PresupuestoMensual;
import com.finanzapp.domain.model.PresupuestoPlantilla;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.AlertaPresupuestoEmitidaEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.BolsilloEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.BolsilloMensualEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.PresupuestoMensualEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.PresupuestoPlantillaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class PresupuestoMapper {

    // -------- Plantilla --------
    public PresupuestoPlantilla toDomain(PresupuestoPlantillaEntity entity) {
        if (entity == null) return null;
        List<Bolsillo> bolsillos = entity.getBolsillos() == null ? new ArrayList<>()
                : entity.getBolsillos().stream().map(this::toDomain).toList();
        return PresupuestoPlantilla.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .tipoBase(entity.getTipoBase())
                .montoFijo(entity.getMontoFijo())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .bolsillos(new ArrayList<>(bolsillos))
                .build();
    }

    public PresupuestoPlantillaEntity toEntity(PresupuestoPlantilla domain) {
        if (domain == null) return null;
        PresupuestoPlantillaEntity entity = PresupuestoPlantillaEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .tipoBase(domain.getTipoBase())
                .montoFijo(domain.getMontoFijo())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .bolsillos(new ArrayList<>())
                .build();
        if (domain.getBolsillos() != null) {
            for (Bolsillo b : domain.getBolsillos()) {
                BolsilloEntity be = toEntity(b);
                be.setPlantilla(entity);
                entity.getBolsillos().add(be);
            }
        }
        return entity;
    }

    public Bolsillo toDomain(BolsilloEntity entity) {
        if (entity == null) return null;
        return Bolsillo.builder()
                .id(entity.getId())
                .plantillaId(entity.getPlantilla() != null ? entity.getPlantilla().getId() : null)
                .nombre(entity.getNombre())
                .porcentaje(entity.getPorcentaje())
                .tipo(entity.getTipo())
                .color(entity.getColor())
                .orden(entity.getOrden())
                .categorias(entity.getCategorias() != null ? new HashSet<>(entity.getCategorias()) : new HashSet<>())
                .build();
    }

    public BolsilloEntity toEntity(Bolsillo domain) {
        if (domain == null) return null;
        return BolsilloEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .porcentaje(domain.getPorcentaje())
                .tipo(domain.getTipo())
                .color(domain.getColor())
                .orden(domain.getOrden() != null ? domain.getOrden() : 0)
                .categorias(domain.getCategorias() != null ? new HashSet<>(domain.getCategorias()) : new HashSet<>())
                .build();
    }

    // -------- Mensual --------
    public PresupuestoMensual toDomain(PresupuestoMensualEntity entity) {
        if (entity == null) return null;
        List<BolsilloMensual> bolsillos = entity.getBolsillos() == null ? new ArrayList<>()
                : entity.getBolsillos().stream().map(this::toDomain).toList();
        return PresupuestoMensual.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .anio(entity.getAnio())
                .mes(entity.getMes())
                .baseCalculada(entity.getBaseCalculada())
                .fechaCalculo(entity.getFechaCalculo())
                .bolsillos(new ArrayList<>(bolsillos))
                .build();
    }

    public PresupuestoMensualEntity toEntity(PresupuestoMensual domain) {
        if (domain == null) return null;
        PresupuestoMensualEntity entity = PresupuestoMensualEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .anio(domain.getAnio())
                .mes(domain.getMes())
                .baseCalculada(domain.getBaseCalculada())
                .fechaCalculo(domain.getFechaCalculo())
                .bolsillos(new ArrayList<>())
                .build();
        if (domain.getBolsillos() != null) {
            for (BolsilloMensual b : domain.getBolsillos()) {
                BolsilloMensualEntity be = toEntity(b);
                be.setPresupuestoMensual(entity);
                entity.getBolsillos().add(be);
            }
        }
        return entity;
    }

    public BolsilloMensual toDomain(BolsilloMensualEntity entity) {
        if (entity == null) return null;
        return BolsilloMensual.builder()
                .id(entity.getId())
                .presupuestoMensualId(entity.getPresupuestoMensual() != null
                        ? entity.getPresupuestoMensual().getId() : null)
                .bolsilloOrigenId(entity.getBolsilloOrigenId())
                .nombre(entity.getNombre())
                .tipo(entity.getTipo())
                .porcentaje(entity.getPorcentaje())
                .montoLimite(entity.getMontoLimite())
                .color(entity.getColor())
                .orden(entity.getOrden())
                .build();
    }

    public BolsilloMensualEntity toEntity(BolsilloMensual domain) {
        if (domain == null) return null;
        return BolsilloMensualEntity.builder()
                .id(domain.getId())
                .bolsilloOrigenId(domain.getBolsilloOrigenId())
                .nombre(domain.getNombre())
                .tipo(domain.getTipo())
                .porcentaje(domain.getPorcentaje())
                .montoLimite(domain.getMontoLimite())
                .color(domain.getColor())
                .orden(domain.getOrden() != null ? domain.getOrden() : 0)
                .build();
    }

    // -------- Alertas --------
    public AlertaPresupuestoEmitida toDomain(AlertaPresupuestoEmitidaEntity entity) {
        if (entity == null) return null;
        return AlertaPresupuestoEmitida.builder()
                .id(entity.getId())
                .bolsilloMensualId(entity.getBolsilloMensualId())
                .nivel(entity.getNivel())
                .fechaEmision(entity.getFechaEmision())
                .build();
    }

    public AlertaPresupuestoEmitidaEntity toEntity(AlertaPresupuestoEmitida domain) {
        if (domain == null) return null;
        return AlertaPresupuestoEmitidaEntity.builder()
                .id(domain.getId())
                .bolsilloMensualId(domain.getBolsilloMensualId())
                .nivel(domain.getNivel())
                .fechaEmision(domain.getFechaEmision())
                .build();
    }
}
