package com.finanzapp.infrastructure.adapter.in.rest.dto.dispositivo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispositivoRequest {

    @NotBlank(message = "El número de WhatsApp es requerido")
    private String numeroWhatsapp;

    private String nombreDispositivo;
}
