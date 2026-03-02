package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.TipoDeuda;
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
@Table(name = "deudas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeudaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDeuda tipo;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 200)
    private String entidad;

    @Column(name = "monto_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "monto_abonado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoAbonado;

    @Column(name = "monto_restante", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoRestante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDeuda estado;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
