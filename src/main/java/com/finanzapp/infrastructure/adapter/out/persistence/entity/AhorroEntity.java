package com.finanzapp.infrastructure.adapter.out.persistence.entity;

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
@Table(name = "ahorros")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AhorroEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "ingreso_id", columnDefinition = "uuid")
    private UUID ingresoId;

    @Column(name = "meta_id", columnDefinition = "uuid")
    private UUID metaId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_id", insertable = false, updatable = false)
    private MetaFinancieraEntity meta;
}
