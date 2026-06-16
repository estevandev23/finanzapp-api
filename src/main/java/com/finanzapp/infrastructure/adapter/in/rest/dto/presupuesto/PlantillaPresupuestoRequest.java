package com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto;

import com.finanzapp.domain.model.TipoBasePresupuesto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaPresupuestoRequest {

    @NotNull
    private TipoBasePresupuesto tipoBase;

    @DecimalMin(value = "0.01", message = "El monto fijo debe ser mayor a cero")
    private BigDecimal montoFijo;

    @Valid
    @NotEmpty(message = "Debes definir al menos un bolsillo")
    private List<BolsilloRequest> bolsillos;
}
