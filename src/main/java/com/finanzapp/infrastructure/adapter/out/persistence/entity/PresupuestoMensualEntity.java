package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "presupuesto_mensual")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoMensualEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", columnDefinition = "uuid", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer mes;

    @Column(name = "base_calculada", nullable = false)
    private BigDecimal baseCalculada;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    @OneToMany(mappedBy = "presupuestoMensual", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<BolsilloMensualEntity> bolsillos = new ArrayList<>();
}
