package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.FrecuenciaRecurrencia;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.TipoRecurrencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recurrencias")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurrenciaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoRecurrencia tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private FrecuenciaRecurrencia frecuencia;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_ingreso", length = 30)
    private CategoriaIngreso categoriaIngreso;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_gasto", length = 30)
    private CategoriaGasto categoriaGasto;

    @Column(name = "categoria_personalizada_id", columnDefinition = "uuid")
    private UUID categoriaPersonalizadaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Column(name = "tarjeta_id", columnDefinition = "uuid")
    private UUID tarjetaId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tarjeta_id", insertable = false, updatable = false)
    private TarjetaCreditoEntity tarjeta;

    @Column(name = "bolsillo_id", columnDefinition = "uuid")
    private UUID bolsilloId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bolsillo_id", insertable = false, updatable = false)
    private BolsilloEntity bolsillo;

    @Column(name = "dia_vencimiento", nullable = false)
    private int diaVencimiento;

    @Column(name = "mes_referencia")
    private Integer mesReferencia;

    @Column(name = "proxima_fecha", nullable = false)
    private LocalDate proximaFecha;

    @Column(name = "ultima_confirmacion_fecha")
    private LocalDate ultimaConfirmacionFecha;

    @Column(nullable = false)
    private boolean activa;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_personalizada_id", insertable = false, updatable = false)
    private CategoriaPersonalizadaEntity categoriaPersonalizada;
}
