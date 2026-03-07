package com.finanzapp.infrastructure.adapter.in.rest.dto.ingreso;

import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.Ingreso;
import com.finanzapp.domain.model.MetodoPago;
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
public class IngresoResponse {
    private UUID id;
    private BigDecimal monto;
    private CategoriaIngreso categoria;
    private String categoriaDescripcion;
    private String categoriaColor;
    private UUID categoriaPersonalizadaId;
    private String descripcion;
    private LocalDate fecha;
    private BigDecimal montoAhorro;
    private BigDecimal montoDisponible;
    private UUID prestamoId;
    private MetodoPago metodoPago;
    private String metodoPagoDescripcion;
    private LocalDateTime fechaCreacion;

    public static IngresoResponse fromDomain(Ingreso ingreso) {
        return IngresoResponse.builder()
                .id(ingreso.getId())
                .monto(ingreso.getMonto())
                .categoria(ingreso.getCategoria())
                .categoriaDescripcion(ingreso.getCategoriaNombre())
                .categoriaColor(ingreso.getCategoriaColor())
                .categoriaPersonalizadaId(ingreso.getCategoriaPersonalizadaId())
                .descripcion(ingreso.getDescripcion())
                .fecha(ingreso.getFecha())
                .montoAhorro(ingreso.getMontoAhorro())
                .montoDisponible(ingreso.getMontoDisponible())
                .prestamoId(ingreso.getPrestamoId())
                .metodoPago(ingreso.getMetodoPago())
                .metodoPagoDescripcion(ingreso.getMetodoPago() != null ? ingreso.getMetodoPago().getDescripcion() : null)
                .fechaCreacion(ingreso.getFechaCreacion())
                .build();
    }
}
