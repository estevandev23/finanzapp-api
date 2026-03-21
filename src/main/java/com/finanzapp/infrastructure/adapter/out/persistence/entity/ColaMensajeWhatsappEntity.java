package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cola_mensajes_whatsapp", indexes = {
    @Index(name = "idx_cola_telefono_procesado", columnList = "numero_telefono, procesado, recibido_en")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColaMensajeWhatsappEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_telefono", nullable = false, length = 20)
    private String numeroTelefono;

    @Column(name = "texto_mensaje", nullable = false, columnDefinition = "TEXT")
    private String textoMensaje;

    @Column(name = "nombre_contacto", length = 100)
    private String nombreContacto;

    @Column(name = "message_id", length = 100)
    private String messageId;

    @Column(name = "es_audio")
    @Builder.Default
    private Boolean esAudio = false;

    @Column(name = "recibido_en", nullable = false)
    @Builder.Default
    private LocalDateTime recibidoEn = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean procesado = false;
}
