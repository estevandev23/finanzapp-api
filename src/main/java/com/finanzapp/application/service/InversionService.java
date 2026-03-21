package com.finanzapp.application.service;

import com.finanzapp.domain.exception.AccesoDenegadoException;
import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.*;
import com.finanzapp.domain.port.in.InversionUseCase;
import com.finanzapp.domain.port.out.GastoRepositoryPort;
import com.finanzapp.domain.port.out.IngresoRepositoryPort;
import com.finanzapp.domain.port.out.InversionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InversionService implements InversionUseCase {

    private final InversionRepositoryPort inversionRepository;
    private final GastoRepositoryPort gastoRepository;
    private final IngresoRepositoryPort ingresoRepository;

    @Override
    public Inversion crear(Inversion inversion) {
        LocalDate fechaInversion = inversion.getFechaInversion() != null
                ? inversion.getFechaInversion()
                : LocalDate.now();

        // Registrar el monto invertido como un gasto con categoría INVERSIONES
        Gasto gasto = Gasto.builder()
                .id(UUID.randomUUID())
                .usuarioId(inversion.getUsuarioId())
                .monto(inversion.getMonto())
                .categoria(CategoriaGasto.INVERSIONES)
                .descripcion("Inversión: " + inversion.getNombre())
                .fecha(fechaInversion)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Gasto gastoCreado = gastoRepository.save(gasto);

        inversion.setId(UUID.randomUUID());
        inversion.setGastoId(gastoCreado.getId());
        inversion.setEstado(EstadoInversion.ACTIVA);
        inversion.setFechaInversion(fechaInversion);
        inversion.setFechaCreacion(LocalDateTime.now());
        inversion.setFechaActualizacion(LocalDateTime.now());

        return inversionRepository.save(inversion);
    }

    @Override
    @Transactional(readOnly = true)
    public Inversion obtenerPorId(UUID id) {
        return inversionRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Inversión", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inversion> listarPorUsuario(UUID usuarioId) {
        return inversionRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inversion> listarPorEstado(UUID usuarioId, EstadoInversion estado) {
        return inversionRepository.findByUsuarioIdAndEstado(usuarioId, estado);
    }

    @Override
    public Inversion registrarRetorno(UUID inversionId, BigDecimal retornoReal, LocalDate fechaRetorno) {
        Inversion inversion = obtenerPorId(inversionId);

        LocalDate fechaRetornoEfectiva = fechaRetorno != null ? fechaRetorno : LocalDate.now();

        // Registrar el retorno como un ingreso con categoría INVERSIONES
        Ingreso ingreso = Ingreso.builder()
                .id(UUID.randomUUID())
                .usuarioId(inversion.getUsuarioId())
                .monto(retornoReal)
                .categoria(CategoriaIngreso.INVERSIONES)
                .descripcion("Retorno de inversión: " + inversion.getNombre())
                .fecha(fechaRetornoEfectiva)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Ingreso ingresoCreado = ingresoRepository.save(ingreso);

        inversion.setIngresoId(ingresoCreado.getId());
        inversion.setRetornoReal(retornoReal);
        inversion.setFechaRetorno(fechaRetornoEfectiva);
        inversion.setEstado(EstadoInversion.FINALIZADA);
        inversion.setFechaActualizacion(LocalDateTime.now());

        return inversionRepository.save(inversion);
    }

    @Override
    public void eliminar(UUID id) {
        Inversion inversion = obtenerPorId(id);

        if (inversion.getIngresoId() != null) {
            ingresoRepository.deleteById(inversion.getIngresoId());
        }

        if (inversion.getGastoId() != null) {
            gastoRepository.deleteById(inversion.getGastoId());
        }

        inversionRepository.deleteById(id);
    }

    public Inversion obtenerPorIdValidado(UUID id, UUID usuarioId) {
        Inversion inversion = obtenerPorId(id);
        validarPropiedad(inversion.getUsuarioId(), usuarioId, "inversion");
        return inversion;
    }

    public void eliminarValidado(UUID id, UUID usuarioId) {
        Inversion inversion = obtenerPorId(id);
        validarPropiedad(inversion.getUsuarioId(), usuarioId, "inversion");
        eliminar(id);
    }

    private void validarPropiedad(UUID propietarioId, UUID solicitanteId, String recurso) {
        if (!propietarioId.equals(solicitanteId)) {
            throw new AccesoDenegadoException(recurso);
        }
    }
}
