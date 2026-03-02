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
public class CategoriaPersonalizada {
    private UUID id;
    private UUID usuarioId;
    private String nombre;
    private TipoCategoria tipo;
    private String color;
    private String icono;
    private boolean activa;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
