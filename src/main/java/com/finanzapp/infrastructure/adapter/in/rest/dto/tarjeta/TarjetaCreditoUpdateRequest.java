package com.finanzapp.infrastructure.adapter.in.rest.dto.tarjeta;

import com.finanzapp.domain.model.EstadoTarjeta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaCreditoUpdateRequest {

    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String banco;

    @Pattern(regexp = "^\\d{4}$", message = "Debe contener exactamente 4 dígitos")
    private String ultimosCuatro;

    @DecimalMin(value = "0.01", message = "El cupo debe ser mayor a 0")
    private BigDecimal cupoTotal;

    @DecimalMin(value = "0.00", message = "El saldo usado no puede ser negativo")
    private BigDecimal cupoUsado;

    @Min(0) @Max(31)
    private int diaCorte;

    @Min(0) @Max(31)
    private int diaPago;

    @Pattern(regexp = "^#?[0-9A-Fa-f]{6}$", message = "Color debe ser hex (#RRGGBB)")
    private String color;

    private EstadoTarjeta estado;
}
