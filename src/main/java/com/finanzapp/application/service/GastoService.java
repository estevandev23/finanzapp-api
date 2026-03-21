package com.finanzapp.application.service;

import com.finanzapp.domain.exception.AccesoDenegadoException;
import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.AbonoDeuda;
import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Deuda;
import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.model.GastoMetodoPago;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.TipoDeuda;
import com.finanzapp.domain.port.in.GastoUseCase;
import com.finanzapp.domain.port.out.AbonoDeudaRepositoryPort;
import com.finanzapp.domain.port.out.DeudaRepositoryPort;
import com.finanzapp.domain.port.out.GastoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GastoService implements GastoUseCase {

    private final GastoRepositoryPort gastoRepository;
    private final DeudaRepositoryPort deudaRepository;
    private final AbonoDeudaRepositoryPort abonoDeudaRepository;

    @Override
    public Gasto registrar(Gasto gasto) {
        gasto.setId(UUID.randomUUID());
        gasto.setFechaCreacion(LocalDateTime.now());
        gasto.setFechaActualizacion(LocalDateTime.now());

        if (gasto.getFecha() == null) {
            gasto.setFecha(LocalDate.now());
        }

        if (gasto.getMetodosPago() == null || gasto.getMetodosPago().isEmpty()) {
            gasto.setMetodosPago(List.of(
                GastoMetodoPago.builder()
                    .id(UUID.randomUUID())
                    .gastoId(gasto.getId())
                    .metodo(MetodoPago.EFECTIVO)
                    .monto(gasto.getMonto())
                    .build()
            ));
        } else {
            gasto.getMetodosPago().forEach(m -> {
                m.setId(UUID.randomUUID());
                m.setGastoId(gasto.getId());
            });
        }

        Gasto guardado = gastoRepository.save(gasto);

        if (guardado.getDeudaId() != null) {
            aplicarAbonoDeudaDesdeGasto(guardado, guardado.getDeudaId());
        }

        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Gasto obtenerPorId(UUID id) {
        return gastoRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Gasto", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gasto> listarPorUsuario(UUID usuarioId) {
        return gastoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gasto> listarPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return gastoRepository.findByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gasto> listarPorCategoria(UUID usuarioId, CategoriaGasto categoria) {
        return gastoRepository.findByUsuarioIdAndCategoria(usuarioId, categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalGastos(UUID usuarioId) {
        BigDecimal total = gastoRepository.sumMontoByUsuarioId(usuarioId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalGastosPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        BigDecimal total = gastoRepository.sumMontoByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<CategoriaGasto, BigDecimal> obtenerDesglosePorCategoria(UUID usuarioId) {
        List<Object[]> resultados = gastoRepository.sumMontoByUsuarioIdGroupByCategoria(usuarioId);
        return mapearResultados(resultados);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<CategoriaGasto, BigDecimal> obtenerDesglosePorCategoriaPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Object[]> resultados = gastoRepository.sumMontoByUsuarioIdAndFechaBetweenGroupByCategoria(usuarioId, fechaInicio, fechaFin);
        return mapearResultados(resultados);
    }

    private Map<CategoriaGasto, BigDecimal> mapearResultados(List<Object[]> resultados) {
        Map<CategoriaGasto, BigDecimal> desglose = new EnumMap<>(CategoriaGasto.class);
        for (Object[] resultado : resultados) {
            CategoriaGasto categoria = (CategoriaGasto) resultado[0];
            if (categoria == null) continue;
            BigDecimal total = (BigDecimal) resultado[1];
            desglose.put(categoria, total);
        }
        return desglose;
    }

    @Override
    public Gasto actualizar(UUID id, Gasto gastoActualizado) {
        Gasto gasto = obtenerPorId(id);
        BigDecimal montoAnterior = gasto.getMonto();

        if (gastoActualizado.getMonto() != null) {
            gasto.setMonto(gastoActualizado.getMonto());
        }
        if (gastoActualizado.getCategoria() != null) {
            gasto.setCategoria(gastoActualizado.getCategoria());
        }
        if (gastoActualizado.getCategoriaPersonalizadaId() != null) {
            gasto.setCategoriaPersonalizadaId(gastoActualizado.getCategoriaPersonalizadaId());
        }
        if (gastoActualizado.getDescripcion() != null) {
            gasto.setDescripcion(gastoActualizado.getDescripcion());
        }
        if (gastoActualizado.getFecha() != null) {
            gasto.setFecha(gastoActualizado.getFecha());
        }
        if (gastoActualizado.getMetodosPago() != null && !gastoActualizado.getMetodosPago().isEmpty()) {
            List<GastoMetodoPago> nuevosMetodos = new ArrayList<>();
            for (GastoMetodoPago mp : gastoActualizado.getMetodosPago()) {
                nuevosMetodos.add(GastoMetodoPago.builder()
                        .id(UUID.randomUUID())
                        .gastoId(gasto.getId())
                        .metodo(mp.getMetodo())
                        .monto(mp.getMonto())
                        .build());
            }
            gasto.setMetodosPago(nuevosMetodos);
        }

        gasto.setFechaActualizacion(LocalDateTime.now());
        Gasto guardado = gastoRepository.save(gasto);

        // Recalcular la deuda si el monto cambió y hay un abono vinculado
        Optional<AbonoDeuda> abonoExistente = abonoDeudaRepository.findByGastoId(id);
        if (abonoExistente.isPresent() && !guardado.getMonto().equals(montoAnterior)) {
            AbonoDeuda abono = abonoExistente.get();
            Deuda deuda = deudaRepository.findById(abono.getDeudaId())
                    .orElseThrow(() -> new RecursoNotFoundException("Deuda", abono.getDeudaId()));
            BigDecimal diferencia = guardado.getMonto().subtract(montoAnterior);
            deuda.setMontoAbonado(deuda.getMontoAbonado().add(diferencia).max(BigDecimal.ZERO));
            deuda.recalcularRestante();
            actualizarEstadoDeuda(deuda);
            deuda.setFechaActualizacion(LocalDateTime.now());
            deudaRepository.save(deuda);

            abono.setMonto(guardado.getMonto());
            abonoDeudaRepository.save(abono);
        }

        return guardado;
    }

    @Override
    public void eliminar(UUID id) {
        Gasto gasto = obtenerPorId(id);

        // Revertir el abono a la deuda si existe
        abonoDeudaRepository.findByGastoId(id).ifPresent(abono -> {
            deudaRepository.findById(abono.getDeudaId()).ifPresent(deuda -> {
                deuda.setMontoAbonado(deuda.getMontoAbonado().subtract(abono.getMonto()).max(BigDecimal.ZERO));
                deuda.recalcularRestante();
                actualizarEstadoDeuda(deuda);
                deuda.setFechaActualizacion(LocalDateTime.now());
                deudaRepository.save(deuda);
            });
            abonoDeudaRepository.deleteById(abono.getId());
        });

        gastoRepository.deleteById(id);
    }

    private void aplicarAbonoDeudaDesdeGasto(Gasto gasto, UUID deudaId) {
        Deuda deuda = deudaRepository.findById(deudaId)
                .orElseThrow(() -> new RecursoNotFoundException("Deuda", deudaId));

        if (deuda.getTipo() != TipoDeuda.DEUDA) {
            throw new DomainException("Solo se puede asociar un gasto a una deuda propia, no a un prestamo");
        }
        if (deuda.getEstado() == EstadoDeuda.COMPLETADA) {
            throw new DomainException("La deuda ya está completada");
        }
        if (gasto.getMonto().compareTo(deuda.getMontoRestante()) > 0) {
            throw new DomainException("El monto del gasto excede el saldo restante de la deuda");
        }

        deuda.setMontoAbonado(deuda.getMontoAbonado().add(gasto.getMonto()));
        deuda.recalcularRestante();
        actualizarEstadoDeuda(deuda);
        deuda.setFechaActualizacion(LocalDateTime.now());
        deudaRepository.save(deuda);

        AbonoDeuda abono = AbonoDeuda.builder()
                .id(UUID.randomUUID())
                .deudaId(deudaId)
                .gastoId(gasto.getId())
                .monto(gasto.getMonto())
                .descripcion(gasto.getDescripcion() != null ? gasto.getDescripcion() : "Abono desde gasto")
                .fechaAbono(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .build();
        abonoDeudaRepository.save(abono);
    }

    public Gasto obtenerPorIdValidado(UUID id, UUID usuarioId) {
        Gasto gasto = obtenerPorId(id);
        validarPropiedad(gasto.getUsuarioId(), usuarioId, "gasto");
        return gasto;
    }

    public Gasto actualizarValidado(UUID id, Gasto gastoActualizado, UUID usuarioId) {
        Gasto gasto = obtenerPorId(id);
        validarPropiedad(gasto.getUsuarioId(), usuarioId, "gasto");
        return actualizar(id, gastoActualizado);
    }

    public void eliminarValidado(UUID id, UUID usuarioId) {
        Gasto gasto = obtenerPorId(id);
        validarPropiedad(gasto.getUsuarioId(), usuarioId, "gasto");
        eliminar(id);
    }

    private void validarPropiedad(UUID propietarioId, UUID solicitanteId, String recurso) {
        if (!propietarioId.equals(solicitanteId)) {
            throw new AccesoDenegadoException(recurso);
        }
    }

    private void actualizarEstadoDeuda(Deuda deuda) {
        if (deuda.isCompletada()) {
            deuda.setEstado(EstadoDeuda.COMPLETADA);
        } else if (deuda.getMontoAbonado().compareTo(BigDecimal.ZERO) > 0) {
            deuda.setEstado(EstadoDeuda.EN_CURSO);
        } else {
            deuda.setEstado(EstadoDeuda.PENDIENTE);
        }
    }
}
