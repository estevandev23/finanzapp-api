package com.finanzapp.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dispositivo {
    private UUID id;
    private UUID usuarioId;
    private String numeroWhatsapp;
    private String nombreDispositivo;
    private String tokenDispositivo;
    private boolean activo;
    private boolean verificado;
    private String codigoVerificacion;
    private LocalDateTime fechaExpiracionCodigo;
    private LocalDateTime ultimaConexion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
