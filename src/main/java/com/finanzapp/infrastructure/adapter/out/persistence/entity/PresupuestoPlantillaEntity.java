package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.TipoBasePresupuesto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "presupuesto_plantilla")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoPlantillaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", columnDefinition = "uuid", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_base", nullable = false, length = 20)
    private TipoBasePresupuesto tipoBase;

    @Column(name = "monto_fijo")
    private BigDecimal montoFijo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "plantilla", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<BolsilloEntity> bolsillos = new ArrayList<>();
}
