package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoMensual {
    private UUID id;
    private UUID usuarioId;
    private Integer anio;
    private Integer mes;
    private BigDecimal baseCalculada;
    private LocalDateTime fechaCalculo;

    @Builder.Default
    private List<BolsilloMensual> bolsillos = new ArrayList<>();
}
