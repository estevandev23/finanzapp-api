package com.finanzapp.infrastructure.adapter.in.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappRegistroRequest {

    @NotBlank(message = "El numero de WhatsApp es requerido")
    private String numeroWhatsapp;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El email es requerido")
    private String email;

    @NotBlank(message = "La contrasena es requerida")
    private String password;
}
