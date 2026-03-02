package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.AbonoDeuda;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AbonoDeudaRepositoryPort {
    AbonoDeuda save(AbonoDeuda abono);
    List<AbonoDeuda> findByDeudaId(UUID deudaId);
    Optional<AbonoDeuda> findByGastoId(UUID gastoId);
    Optional<AbonoDeuda> findByIngresoId(UUID ingresoId);
    void deleteById(UUID id);
    void deleteAllByDeudaId(UUID deudaId);
}
