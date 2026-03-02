package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "abonos_deuda")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbonoDeudaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deuda_id", nullable = false)
    private DeudaEntity deuda;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_abono", nullable = false)
    private LocalDateTime fechaAbono;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "gasto_id", columnDefinition = "uuid")
    private UUID gastoId;

    @Column(name = "ingreso_id", columnDefinition = "uuid")
    private UUID ingresoId;
}
