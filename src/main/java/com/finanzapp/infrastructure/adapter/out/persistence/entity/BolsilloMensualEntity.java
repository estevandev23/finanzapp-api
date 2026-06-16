package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.TipoBolsillo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bolsillo_mensual")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"presupuestoMensual"})
public class BolsilloMensualEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_mensual_id", nullable = false)
    private PresupuestoMensualEntity presupuestoMensual;

    @Column(name = "bolsillo_origen_id", columnDefinition = "uuid")
    private UUID bolsilloOrigenId;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoBolsillo tipo;

    @Column(nullable = false)
    private BigDecimal porcentaje;

    @Column(name = "monto_limite", nullable = false)
    private BigDecimal montoLimite;

    @Column(length = 7)
    private String color;

    @Column(nullable = false)
    private Integer orden;
}
