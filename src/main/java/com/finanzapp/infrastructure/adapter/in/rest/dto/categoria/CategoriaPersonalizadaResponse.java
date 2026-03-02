package com.finanzapp.infrastructure.adapter.in.rest.dto.categoria;

import com.finanzapp.domain.model.CategoriaPersonalizada;
import com.finanzapp.domain.model.TipoCategoria;
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
public class CategoriaPersonalizadaResponse {
    private UUID id;
    private String nombre;
    private TipoCategoria tipo;
    private String color;
    private String icono;
    private boolean activa;
    private LocalDateTime fechaCreacion;

    public static CategoriaPersonalizadaResponse fromDomain(CategoriaPersonalizada categoria) {
        return CategoriaPersonalizadaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .tipo(categoria.getTipo())
                .color(categoria.getColor())
                .icono(categoria.getIcono())
                .activa(categoria.isActiva())
                .fechaCreacion(categoria.getFechaCreacion())
                .build();
    }
}
