package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.Deuda;
import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.TipoDeuda;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeudaRepositoryPort {
    Deuda save(Deuda deuda);
    Optional<Deuda> findById(UUID id);
    List<Deuda> findByUsuarioId(UUID usuarioId);
    List<Deuda> findByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo);
    List<Deuda> findByUsuarioIdAndEstado(UUID usuarioId, EstadoDeuda estado);
    BigDecimal sumMontoRestanteByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo);
    BigDecimal sumMontoAbonadoByUsuarioIdAndTipo(UUID usuarioId, TipoDeuda tipo);
    void deleteById(UUID id);
}
