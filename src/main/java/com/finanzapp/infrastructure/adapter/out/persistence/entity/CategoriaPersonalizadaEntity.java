package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.TipoCategoria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categorias_personalizadas",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "nombre", "tipo"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaPersonalizadaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoCategoria tipo;

    @Column(length = 7)
    private String color;

    @Column(length = 50)
    private String icono;

    @Column(nullable = false)
    private boolean activa;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private UsuarioEntity usuario;
}
