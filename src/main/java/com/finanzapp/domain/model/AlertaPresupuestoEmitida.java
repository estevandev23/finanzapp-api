package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaPresupuestoEmitida {
    private UUID id;
    private UUID bolsilloMensualId;
    private NivelAlertaPresupuesto nivel;
    private LocalDateTime fechaEmision;
}
