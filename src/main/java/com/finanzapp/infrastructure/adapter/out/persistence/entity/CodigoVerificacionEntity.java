package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.TipoVerificacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "codigos_verificacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoVerificacionEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(nullable = false, length = 6)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoVerificacion tipo;

    @Column(nullable = false)
    private boolean usado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;
}
