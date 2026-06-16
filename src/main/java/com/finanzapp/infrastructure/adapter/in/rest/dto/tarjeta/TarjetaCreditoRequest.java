package com.finanzapp.infrastructure.adapter.in.rest.dto.tarjeta;

import com.finanzapp.domain.model.EstadoTarjeta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaCreditoRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String banco;

    @Pattern(regexp = "^\\d{4}$", message = "Debe contener exactamente 4 dígitos")
    private String ultimosCuatro;

    @NotNull(message = "El cupo total es requerido")
    @DecimalMin(value = "0.01", message = "El cupo debe ser mayor a 0")
    private BigDecimal cupoTotal;

    @Min(1) @Max(31)
    private int diaCorte;

    @Min(1) @Max(31)
    private int diaPago;

    @Pattern(regexp = "^#?[0-9A-Fa-f]{6}$", message = "Color debe ser hex (#RRGGBB)")
    private String color;

    private EstadoTarjeta estado;
}
