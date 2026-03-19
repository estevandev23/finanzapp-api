package com.finanzapp.infrastructure.adapter.in.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappVerificarCodigoRequest {

    @NotBlank(message = "El numero de WhatsApp es requerido")
    private String numeroWhatsapp;

    @NotBlank(message = "El codigo de verificacion es requerido")
    private String codigo;
}
