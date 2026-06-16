package com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.TipoBolsillo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BolsilloRequest {

    /**
     * Id del bolsillo existente. Si se envía, se conserva para no romper las
     * asociaciones de gastos y recurrencias; si es null se trata como bolsillo nuevo.
     */
    private UUID id;

    @NotBlank
    @Size(max = 80)
    private String nombre;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "100.00")
    private BigDecimal porcentaje;

    @NotNull
    private TipoBolsillo tipo;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "El color debe tener formato hex #RRGGBB")
    private String color;

    private Integer orden;

    private Set<CategoriaGasto> categorias = new HashSet<>();
}
