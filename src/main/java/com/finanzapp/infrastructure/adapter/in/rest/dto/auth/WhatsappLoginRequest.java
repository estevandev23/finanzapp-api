package com.finanzapp.infrastructure.adapter.in.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappLoginRequest {

    @NotBlank(message = "El número de WhatsApp es requerido")
    private String numeroWhatsapp;

    @NotBlank(message = "El código de verificación es requerido")
    private String codigoVerificacion;
}
