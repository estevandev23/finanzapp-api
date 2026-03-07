package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.MetodoPago;
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
@Table(name = "ingresos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngresoEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private CategoriaIngreso categoria;

    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "monto_ahorro", precision = 15, scale = 2)
    private BigDecimal montoAhorro;

    @Column(name = "categoria_personalizada_id", columnDefinition = "uuid")
    private UUID categoriaPersonalizadaId;

    @Column(name = "prestamo_id", columnDefinition = "uuid")
    private UUID prestamoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

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
}
