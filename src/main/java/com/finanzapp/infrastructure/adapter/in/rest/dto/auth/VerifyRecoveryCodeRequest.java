package com.finanzapp.infrastructure.adapter.in.rest.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRecoveryCodeRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser valido")
    private String email;

    @NotBlank(message = "El codigo es requerido")
    @Size(min = 6, max = 6, message = "El codigo debe tener exactamente 6 digitos")
    private String codigo;
}
