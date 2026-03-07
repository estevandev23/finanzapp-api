package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.CategoriaGasto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "gastos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private CategoriaGasto categoria;

    @Column(name = "categoria_personalizada_id", columnDefinition = "uuid")
    private UUID categoriaPersonalizadaId;

    @Column(name = "deuda_id", columnDefinition = "uuid")
    private UUID deudaId;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_personalizada_id", insertable = false, updatable = false)
    private CategoriaPersonalizadaEntity categoriaPersonalizada;

    @OneToMany(mappedBy = "gasto", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GastoMetodoPagoEntity> metodosPago;
}
