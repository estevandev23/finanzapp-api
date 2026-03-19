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
public class SesionWhatsapp {
    private UUID id;
    private String numeroWhatsapp;
    private UUID usuarioId;
    private String token;
    private String refreshToken;
    private boolean activa;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime ultimaActividad;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
