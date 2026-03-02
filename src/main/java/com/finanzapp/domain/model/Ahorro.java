package com.finanzapp.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ahorro {
    private UUID id;
    private UUID usuarioId;
    private UUID ingresoId;
    private UUID metaId;
    private BigDecimal monto;
    private String descripcion;
    private LocalDate fecha;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
