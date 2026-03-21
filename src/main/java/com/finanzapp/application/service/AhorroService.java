package com.finanzapp.application.service;

import com.finanzapp.domain.exception.AccesoDenegadoException;
import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.Ahorro;
import com.finanzapp.domain.port.in.AhorroUseCase;
import com.finanzapp.domain.port.out.AhorroRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AhorroService implements AhorroUseCase {

    private final AhorroRepositoryPort ahorroRepository;

    @Override
    public Ahorro registrar(Ahorro ahorro) {
        ahorro.setId(UUID.randomUUID());
        ahorro.setFechaCreacion(LocalDateTime.now());
        ahorro.setFechaActualizacion(LocalDateTime.now());

        if (ahorro.getFecha() == null) {
            ahorro.setFecha(LocalDate.now());
        }

        return ahorroRepository.save(ahorro);
    }

    @Override
    @Transactional(readOnly = true)
    public Ahorro obtenerPorId(UUID id) {
        return ahorroRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Ahorro", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ahorro> listarPorUsuario(UUID usuarioId) {
        return ahorroRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ahorro> listarPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return ahorroRepository.findByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ahorro> listarPorMeta(UUID metaId) {
        return ahorroRepository.findByMetaId(metaId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalAhorros(UUID usuarioId) {
        BigDecimal total = ahorroRepository.sumMontoByUsuarioId(usuarioId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalAhorrosPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        BigDecimal total = ahorroRepository.sumMontoByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Ahorro actualizar(UUID id, Ahorro ahorroActualizado) {
        Ahorro ahorro = obtenerPorId(id);

        if (ahorroActualizado.getMonto() != null) {
            ahorro.setMonto(ahorroActualizado.getMonto());
        }
        if (ahorroActualizado.getDescripcion() != null) {
            ahorro.setDescripcion(ahorroActualizado.getDescripcion());
        }
        if (ahorroActualizado.getFecha() != null) {
            ahorro.setFecha(ahorroActualizado.getFecha());
        }
        if (ahorroActualizado.getMetaId() != null) {
            ahorro.setMetaId(ahorroActualizado.getMetaId());
        }

        ahorro.setFechaActualizacion(LocalDateTime.now());
        return ahorroRepository.save(ahorro);
    }

    @Override
    public void eliminar(UUID id) {
        obtenerPorId(id);
        ahorroRepository.deleteById(id);
    }

    public Ahorro obtenerPorIdValidado(UUID id, UUID usuarioId) {
        Ahorro ahorro = obtenerPorId(id);
        validarPropiedad(ahorro.getUsuarioId(), usuarioId, "ahorro");
        return ahorro;
    }

    public Ahorro actualizarValidado(UUID id, Ahorro ahorroActualizado, UUID usuarioId) {
        Ahorro ahorro = obtenerPorId(id);
        validarPropiedad(ahorro.getUsuarioId(), usuarioId, "ahorro");
        return actualizar(id, ahorroActualizado);
    }

    public void eliminarValidado(UUID id, UUID usuarioId) {
        Ahorro ahorro = obtenerPorId(id);
        validarPropiedad(ahorro.getUsuarioId(), usuarioId, "ahorro");
        eliminar(id);
    }

    private void validarPropiedad(UUID propietarioId, UUID solicitanteId, String recurso) {
        if (!propietarioId.equals(solicitanteId)) {
            throw new AccesoDenegadoException(recurso);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ahorro> buscarPorIngresoId(UUID ingresoId) {
        return ahorroRepository.findByIngresoId(ingresoId);
    }
}
