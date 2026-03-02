package com.finanzapp.infrastructure.adapter.in.rest.dto.usuario;

import com.finanzapp.domain.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private UUID id;
    private String nombre;
    private String email;
    private String telefono;
    private boolean activo;
    private boolean dosFactoresActivado;
    private boolean telefonoVerificado;
    private String oauthProvider;
    private LocalDateTime fechaCreacion;

    public static UsuarioResponse fromDomain(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .activo(usuario.isActivo())
                .dosFactoresActivado(usuario.isDosFactoresActivado())
                .telefonoVerificado(usuario.isTelefonoVerificado())
                .oauthProvider(usuario.getOauthProvider())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }
}
