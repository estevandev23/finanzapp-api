package com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarRecurrenciaResponse {
    private UUID recurrenciaId;
    private UUID registroGeneradoId;
    private String mensaje;
}
