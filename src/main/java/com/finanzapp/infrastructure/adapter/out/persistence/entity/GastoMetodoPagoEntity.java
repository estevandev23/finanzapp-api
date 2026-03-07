package com.finanzapp.infrastructure.adapter.out.persistence.entity;

import com.finanzapp.domain.model.MetodoPago;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "gasto_metodo_pago")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoMetodoPagoEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gasto_id", nullable = false)
    private GastoEntity gasto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPago metodo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    public UUID getGastoId() {
        return gasto != null ? gasto.getId() : null;
    }
}
