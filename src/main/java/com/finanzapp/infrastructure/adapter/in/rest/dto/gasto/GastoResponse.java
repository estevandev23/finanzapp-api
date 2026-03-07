package com.finanzapp.infrastructure.adapter.in.rest.dto.gasto;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.model.GastoMetodoPago;
import com.finanzapp.domain.model.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    private List<MetodoPagoDetalleResponse> metodosPago;
    private LocalDateTime fechaCreacion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetodoPagoDetalleResponse {
        private UUID id;
        private MetodoPago metodo;
        private String metodoDescripcion;
        private BigDecimal monto;

        public static MetodoPagoDetalleResponse from(GastoMetodoPago detalle) {
            return MetodoPagoDetalleResponse.builder()
                    .id(detalle.getId())
                    .metodo(detalle.getMetodo())
                    .metodoDescripcion(detalle.getMetodo().getDescripcion())
                    .monto(detalle.getMonto())
                    .build();
        }
    }

    public static GastoResponse fromDomain(Gasto gasto) {
        List<MetodoPagoDetalleResponse> metodos = gasto.getMetodosPago() != null
                ? gasto.getMetodosPago().stream().map(MetodoPagoDetalleResponse::from).toList()
                : Collections.emptyList();

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
                .metodosPago(metodos)
                .fechaCreacion(gasto.getFechaCreacion())
                .build();
    }
}
