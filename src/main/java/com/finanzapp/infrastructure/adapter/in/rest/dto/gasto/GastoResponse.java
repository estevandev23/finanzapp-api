package com.finanzapp.infrastructure.adapter.in.rest.dto.gasto;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Gasto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoResponse {
    private UUID id;
    private BigDecimal monto;
    private CategoriaGasto categoria;
    private String categoriaDescripcion;
    private String categoriaColor;
    private UUID categoriaPersonalizadaId;
    private UUID deudaId;
    private String descripcion;
    private LocalDate fecha;
    private LocalDateTime fechaCreacion;

    public static GastoResponse fromDomain(Gasto gasto) {
        return GastoResponse.builder()
                .id(gasto.getId())
                .monto(gasto.getMonto())
                .categoria(gasto.getCategoria())
                .categoriaDescripcion(gasto.getCategoriaNombre())
                .categoriaColor(gasto.getCategoriaColor())
                .categoriaPersonalizadaId(gasto.getCategoriaPersonalizadaId())
                .deudaId(gasto.getDeudaId())
                .descripcion(gasto.getDescripcion())
                .fecha(gasto.getFecha())
                .fechaCreacion(gasto.getFechaCreacion())
                .build();
    }
}
