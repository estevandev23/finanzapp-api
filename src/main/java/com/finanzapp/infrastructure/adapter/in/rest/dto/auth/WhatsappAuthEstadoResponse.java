package com.finanzapp.infrastructure.adapter.in.rest.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappAuthEstadoResponse {
    private boolean sesionActiva;
    private boolean cuentaExiste;
    private boolean dispositivoRegistrado;
    private boolean dispositivoVerificado;
    private String mensaje;
}
