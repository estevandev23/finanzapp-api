package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sesiones_whatsapp")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionWhatsappEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "numero_whatsapp", nullable = false, unique = true)
    private String numeroWhatsapp;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(nullable = false, length = 1024)
    private String token;

    @Column(name = "refresh_token", length = 1024)
    private String refreshToken;

    @Column(nullable = false)
    private boolean activa;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "ultima_actividad")
    private LocalDateTime ultimaActividad;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private UsuarioEntity usuario;
}
