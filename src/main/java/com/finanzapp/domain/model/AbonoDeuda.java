package com.finanzapp.domain.model;

import java.math.BigDecimal;
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
public class AbonoDeuda {
    private UUID id;
    private UUID deudaId;
    private UUID gastoId;
    private UUID ingresoId;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fechaAbono;
    private LocalDateTime fechaCreacion;
}
