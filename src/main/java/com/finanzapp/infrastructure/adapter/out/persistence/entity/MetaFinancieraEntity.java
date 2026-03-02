package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.EstadoMeta;
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
@Table(name = "metas_financieras")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaFinancieraEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "monto_objetivo", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoObjetivo;

    @Column(name = "monto_actual", precision = 15, scale = 2)
    private BigDecimal montoActual;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMeta estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private UsuarioEntity usuario;
}
