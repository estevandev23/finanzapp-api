package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.TipoBolsillo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "bolsillo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"plantilla"})
public class BolsilloEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_id", nullable = false)
    private PresupuestoPlantillaEntity plantilla;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private BigDecimal porcentaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoBolsillo tipo;

    @Column(length = 7)
    private String color;

    @Column(nullable = false)
    private Integer orden;

    @ElementCollection(targetClass = CategoriaGasto.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "bolsillo_categoria",
            joinColumns = @JoinColumn(name = "bolsillo_id"))
    @Column(name = "categoria", length = 40)
    @Builder.Default
    private Set<CategoriaGasto> categorias = new HashSet<>();
}
