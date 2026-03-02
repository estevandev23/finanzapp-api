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
public class Usuario {
    private UUID id;
    private String nombre;
    private String email;
    private String password;
    private String telefono;
    private String oauthProvider;
    private String oauthProviderId;
    private boolean dosFactoresActivado;
    private boolean telefonoVerificado;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
