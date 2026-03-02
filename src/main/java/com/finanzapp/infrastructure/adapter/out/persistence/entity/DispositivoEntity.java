package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dispositivos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispositivoEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "numero_whatsapp", nullable = false)
    private String numeroWhatsapp;

    @Column(name = "nombre_dispositivo")
    private String nombreDispositivo;

    @Column(name = "token_dispositivo", unique = true)
    private String tokenDispositivo;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private boolean verificado;

    @Column(name = "codigo_verificacion")
    private String codigoVerificacion;

    @Column(name = "fecha_expiracion_codigo")
    private LocalDateTime fechaExpiracionCodigo;

    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private UsuarioEntity usuario;
}
