package com.finanzapp.infrastructure.adapter.in.rest.dto.categoria;

import com.finanzapp.domain.model.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaPersonalizadaRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotNull(message = "El tipo es requerido (INGRESO o GASTO)")
    private TipoCategoria tipo;

    @Size(max = 7, message = "El color debe ser un código hex válido (ej: #FF5733)")
    private String color;

    @Size(max = 50, message = "El icono no debe superar 50 caracteres")
    private String icono;
}
