package com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarRecurrenciaRequest {

    /** Fecha con la que se registra el ingreso o gasto. Si es null se usa la fecha actual. */
    private LocalDate fechaConfirmacion;
}
