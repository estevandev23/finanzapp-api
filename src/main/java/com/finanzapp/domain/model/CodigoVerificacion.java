package com.finanzapp.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoVerificacion {
    private UUID id;
    private UUID usuarioId;
    private String codigo;
    private TipoVerificacion tipo;
    private boolean usado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
}
