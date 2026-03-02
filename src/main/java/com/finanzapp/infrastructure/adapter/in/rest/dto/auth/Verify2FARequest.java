package com.finanzapp.infrastructure.adapter.in.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Verify2FARequest {

    @NotNull(message = "El ID del usuario es requerido")
    private UUID usuarioId;

    @NotBlank(message = "El código de verificación es requerido")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
    private String codigo;
}
