package com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto;

import com.finanzapp.domain.model.Bolsillo;
import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.TipoBolsillo;
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
public class BolsilloResponse {
    private UUID id;
    private String nombre;
    private BigDecimal porcentaje;
    private TipoBolsillo tipo;
    private String tipoDescripcion;
    private String color;
    private Integer orden;
    private Set<CategoriaGasto> categorias;

    public static BolsilloResponse fromDomain(Bolsillo b) {
        return BolsilloResponse.builder()
                .id(b.getId())
                .nombre(b.getNombre())
                .porcentaje(b.getPorcentaje())
                .tipo(b.getTipo())
                .tipoDescripcion(b.getTipo() != null ? b.getTipo().getDescripcion() : null)
                .color(b.getColor())
                .orden(b.getOrden())
                .categorias(b.getCategorias() != null ? new HashSet<>(b.getCategorias()) : new HashSet<>())
                .build();
    }
}
