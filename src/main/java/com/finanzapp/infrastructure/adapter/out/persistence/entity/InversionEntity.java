package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.EstadoInversion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inversiones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InversionEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "gasto_id", columnDefinition = "uuid")
    private UUID gastoId;

    @Column(name = "ingreso_id", columnDefinition = "uuid")
    private UUID ingresoId;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "retorno_esperado", precision = 15, scale = 2)
    private BigDecimal retornoEsperado;

    @Column(name = "retorno_real", precision = 15, scale = 2)
    private BigDecimal retornoReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInversion estado;

    @Column(name = "fecha_inversion", nullable = false)
    private LocalDate fechaInversion;

    @Column(name = "fecha_retorno")
    private LocalDate fechaRetorno;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
