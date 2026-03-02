package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "prefijos_pais")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrefijoPaisEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "codigo_iso", nullable = false, length = 3, unique = true)
    private String codigoIso;

    @Column(name = "prefijo_telefono", nullable = false, length = 10)
    private String prefijoTelefono;

    @Column(name = "bandera_emoji", length = 10)
    private String banderaEmoji;

    @Column(nullable = false)
    private boolean activo;
}
