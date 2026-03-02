package com.finanzapp.infrastructure.adapter.in.rest.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthRequest {

    @NotBlank(message = "El proveedor OAuth es requerido")
    private String provider;

    @NotBlank(message = "El ID del proveedor es requerido")
    private String providerId;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;

    private String nombre;
}
