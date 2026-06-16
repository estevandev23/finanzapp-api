package com.finanzapp.infrastructure.adapter.in.rest.dto.tarjeta;

import com.finanzapp.domain.model.EstadoTarjeta;
import com.finanzapp.domain.model.TarjetaCredito;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaCreditoResponse {
    private UUID id;
    private String nombre;
    private String banco;
    private String ultimosCuatro;
    private BigDecimal cupoTotal;
    private BigDecimal cupoUsado;
    private BigDecimal cupoDisponible;
    private BigDecimal porcentajeUtilizacion;
    private int diaCorte;
    private int diaPago;
    private String color;
    private EstadoTarjeta estado;
    private String estadoDescripcion;
    private LocalDateTime fechaCreacion;

    public static TarjetaCreditoResponse fromDomain(TarjetaCredito tarjeta) {
        return TarjetaCreditoResponse.builder()
                .id(tarjeta.getId())
                .nombre(tarjeta.getNombre())
                .banco(tarjeta.getBanco())
                .ultimosCuatro(tarjeta.getUltimosCuatro())
                .cupoTotal(tarjeta.getCupoTotal())
                .cupoUsado(tarjeta.getCupoUsado())
                .cupoDisponible(tarjeta.getCupoDisponible())
                .porcentajeUtilizacion(tarjeta.getPorcentajeUtilizacion())
                .diaCorte(tarjeta.getDiaCorte())
                .diaPago(tarjeta.getDiaPago())
                .color(tarjeta.getColor())
                .estado(tarjeta.getEstado())
                .estadoDescripcion(tarjeta.getEstado() != null ? tarjeta.getEstado().getDescripcion() : null)
                .fechaCreacion(tarjeta.getFechaCreacion())
                .build();
    }
}
