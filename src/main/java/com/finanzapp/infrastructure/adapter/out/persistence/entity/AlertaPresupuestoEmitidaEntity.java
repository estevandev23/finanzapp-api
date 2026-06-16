package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.NivelAlertaPresupuesto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerta_presupuesto_emitida")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaPresupuestoEmitidaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "bolsillo_mensual_id", columnDefinition = "uuid", nullable = false)
    private UUID bolsilloMensualId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelAlertaPresupuesto nivel;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;
}
