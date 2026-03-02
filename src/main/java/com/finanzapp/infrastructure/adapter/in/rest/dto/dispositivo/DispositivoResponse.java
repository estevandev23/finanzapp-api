package com.finanzapp.infrastructure.adapter.in.rest.dto.dispositivo;

import com.finanzapp.domain.model.Dispositivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispositivoResponse {
    private UUID id;
    private String numeroWhatsapp;
    private String nombreDispositivo;
    private boolean activo;
    private boolean verificado;
    private String codigoVerificacion;
    private LocalDateTime ultimaConexion;
    private LocalDateTime fechaCreacion;

    public static DispositivoResponse fromDomain(Dispositivo dispositivo) {
        return DispositivoResponse.builder()
                .id(dispositivo.getId())
                .numeroWhatsapp(dispositivo.getNumeroWhatsapp())
                .nombreDispositivo(dispositivo.getNombreDispositivo())
                .activo(dispositivo.isActivo())
                .verificado(dispositivo.isVerificado())
                .codigoVerificacion(dispositivo.getCodigoVerificacion())
                .ultimaConexion(dispositivo.getUltimaConexion())
                .fechaCreacion(dispositivo.getFechaCreacion())
                .build();
    }
}
