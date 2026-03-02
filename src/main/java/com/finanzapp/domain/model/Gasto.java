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
public class Gasto {
    private UUID id;
    private UUID usuarioId;
    private BigDecimal monto;
    private CategoriaGasto categoria;
    private UUID categoriaPersonalizadaId;
    private String categoriaNombre;
    private String categoriaColor;
    private UUID deudaId;
    private String descripcion;
    private LocalDate fecha;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
