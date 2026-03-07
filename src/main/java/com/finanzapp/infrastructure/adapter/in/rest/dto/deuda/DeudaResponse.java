package com.finanzapp.infrastructure.adapter.in.rest.dto.deuda;

import com.finanzapp.domain.model.Deuda;
import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.TipoDeuda;
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
public class DeudaResponse {
    private UUID id;
    private TipoDeuda tipo;
    private String tipoDescripcion;
    private String descripcion;
    private String entidad;
    private String categoria;
    private UUID categoriaPersonalizadaId;
    private String categoriaDescripcion;
    private String categoriaColor;
    private BigDecimal montoTotal;
    private BigDecimal montoAbonado;
    private BigDecimal montoRestante;
    private BigDecimal porcentajeAvance;
    private EstadoDeuda estado;
    private String estadoDescripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaLimite;
    private LocalDateTime fechaCreacion;

    public static DeudaResponse fromDomain(Deuda deuda) {
        return DeudaResponse.builder()
                .id(deuda.getId())
                .tipo(deuda.getTipo())
                .tipoDescripcion(deuda.getTipo().getDescripcion())
                .descripcion(deuda.getDescripcion())
                .entidad(deuda.getEntidad())
                .categoria(deuda.getCategoria())
                .categoriaPersonalizadaId(deuda.getCategoriaPersonalizadaId())
                .categoriaDescripcion(deuda.getCategoriaDescripcion())
                .categoriaColor(deuda.getCategoriaColor())
                .montoTotal(deuda.getMontoTotal())
                .montoAbonado(deuda.getMontoAbonado())
                .montoRestante(deuda.getMontoRestante())
                .porcentajeAvance(deuda.getPorcentajeAvance())
                .estado(deuda.getEstado())
                .estadoDescripcion(deuda.getEstado().getDescripcion())
                .fechaInicio(deuda.getFechaInicio())
                .fechaLimite(deuda.getFechaLimite())
                .fechaCreacion(deuda.getFechaCreacion())
                .build();
    }
}
