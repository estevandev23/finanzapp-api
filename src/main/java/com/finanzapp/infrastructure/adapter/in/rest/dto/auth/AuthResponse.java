package com.finanzapp.infrastructure.adapter.in.rest.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UUID usuarioId;
    private String nombre;
    private String email;
    private boolean requiere2FA;
    private UUID verificacionId;
}
