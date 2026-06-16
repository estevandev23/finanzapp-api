package com.finanzapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bolsillo {
    private UUID id;
    private UUID plantillaId;
    private String nombre;
    private BigDecimal porcentaje;
    private TipoBolsillo tipo;
    private String color;
    private Integer orden;

    @Builder.Default
    private Set<CategoriaGasto> categorias = new HashSet<>();
}
