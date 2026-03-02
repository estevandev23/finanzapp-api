package com.finanzapp.infrastructure.adapter.in.rest.dto.usuario;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateRequest {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Size(min = 10, max = 15, message = "El teléfono debe tener entre 10 y 15 caracteres")
    private String telefono;
}
