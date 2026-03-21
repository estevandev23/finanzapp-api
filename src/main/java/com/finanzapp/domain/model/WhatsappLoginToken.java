package com.finanzapp.domain.model;

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
public class WhatsappLoginToken {
    private UUID id;
    private String token;
    private String numeroWhatsapp;
    private boolean usado;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaCreacion;
}
