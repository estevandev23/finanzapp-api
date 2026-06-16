package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.EstadoTarjeta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tarjetas_credito")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaCreditoEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String banco;

    @Column(name = "ultimos_cuatro", length = 4)
    private String ultimosCuatro;

    @Column(name = "cupo_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal cupoTotal;

    @Column(name = "cupo_usado", nullable = false, precision = 15, scale = 2)
    private BigDecimal cupoUsado;

    @Column(name = "dia_corte", nullable = false)
    private int diaCorte;

    @Column(name = "dia_pago", nullable = false)
    private int diaPago;

    @Column(length = 7)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoTarjeta estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
