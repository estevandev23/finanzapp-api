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
public class ResetPasswordRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser valido")
    private String email;

    @NotBlank(message = "El codigo de recuperacion es requerido")
    @Size(min = 6, max = 6, message = "El codigo debe tener 6 digitos")
    private String codigo;

    @NotBlank(message = "La nueva contrasena es requerida")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String nuevaPassword;
}
