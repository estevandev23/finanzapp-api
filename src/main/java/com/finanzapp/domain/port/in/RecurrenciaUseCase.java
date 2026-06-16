package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoRecurrencia;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecurrenciaUseCase {

    Recurrencia crear(Recurrencia recurrencia);

    Recurrencia obtenerPorId(UUID id);

    List<Recurrencia> listarPorUsuario(UUID usuarioId);

    List<Recurrencia> listarPorTipo(UUID usuarioId, TipoRecurrencia tipo);

    List<Recurrencia> listarActivasPorUsuario(UUID usuarioId);

    Recurrencia actualizar(UUID id, Recurrencia recurrencia);

    Recurrencia cambiarEstado(UUID id, boolean activa);

    void eliminar(UUID id);

    /**
     * Confirma la recurrencia: genera el ingreso o gasto real con la fecha indicada
     * (o la fecha actual si es null) y avanza la próxima fecha según la frecuencia.
     */
    UUID confirmar(UUID recurrenciaId, LocalDate fechaConfirmacion);
}
