package com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.FrecuenciaRecurrencia;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoRecurrencia;
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
public class RecurrenciaResponse {
    private UUID id;
    private TipoRecurrencia tipo;
    private String tipoDescripcion;
    private FrecuenciaRecurrencia frecuencia;
    private String frecuenciaDescripcion;
    private String descripcion;
    private BigDecimal monto;
    private CategoriaIngreso categoriaIngreso;
    private CategoriaGasto categoriaGasto;
    private UUID categoriaPersonalizadaId;
    private String categoriaNombre;
    private String categoriaColor;
    private MetodoPago metodoPago;
    private String metodoPagoDescripcion;
    private UUID tarjetaId;
    private String tarjetaNombre;
    private UUID bolsilloId;
    private String bolsilloNombre;
    private int diaVencimiento;
    private Integer mesReferencia;
    private LocalDate proximaFecha;
    private LocalDate ultimaConfirmacionFecha;
    private long diasParaVencer;
    private boolean vencida;
    private boolean activa;
    private LocalDateTime fechaCreacion;

    public static RecurrenciaResponse fromDomain(Recurrencia recurrencia) {
        LocalDate hoy = LocalDate.now();
        return RecurrenciaResponse.builder()
                .id(recurrencia.getId())
                .tipo(recurrencia.getTipo())
                .tipoDescripcion(recurrencia.getTipo() != null ? recurrencia.getTipo().getDescripcion() : null)
                .frecuencia(recurrencia.getFrecuencia())
                .frecuenciaDescripcion(recurrencia.getFrecuencia() != null ? recurrencia.getFrecuencia().getDescripcion() : null)
                .descripcion(recurrencia.getDescripcion())
                .monto(recurrencia.getMonto())
                .categoriaIngreso(recurrencia.getCategoriaIngreso())
                .categoriaGasto(recurrencia.getCategoriaGasto())
                .categoriaPersonalizadaId(recurrencia.getCategoriaPersonalizadaId())
                .categoriaNombre(recurrencia.getCategoriaNombre())
                .categoriaColor(recurrencia.getCategoriaColor())
                .metodoPago(recurrencia.getMetodoPago())
                .metodoPagoDescripcion(recurrencia.getMetodoPago() != null ? recurrencia.getMetodoPago().getDescripcion() : null)
                .tarjetaId(recurrencia.getTarjetaId())
                .tarjetaNombre(recurrencia.getTarjetaNombre())
                .bolsilloId(recurrencia.getBolsilloId())
                .bolsilloNombre(recurrencia.getBolsilloNombre())
                .diaVencimiento(recurrencia.getDiaVencimiento())
                .mesReferencia(recurrencia.getMesReferencia())
                .proximaFecha(recurrencia.getProximaFecha())
                .ultimaConfirmacionFecha(recurrencia.getUltimaConfirmacionFecha())
                .diasParaVencer(recurrencia.diasParaVencer(hoy))
                .vencida(recurrencia.isVencida(hoy))
                .activa(recurrencia.isActiva())
                .fechaCreacion(recurrencia.getFechaCreacion())
                .build();
    }
}
