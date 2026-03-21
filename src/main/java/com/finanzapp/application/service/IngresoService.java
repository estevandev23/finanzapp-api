package com.finanzapp.application.service;

import com.finanzapp.domain.exception.AccesoDenegadoException;
import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.Ahorro;
import com.finanzapp.domain.model.AbonoDeuda;
import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.Deuda;
import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.Ingreso;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.TipoDeuda;
import com.finanzapp.domain.port.in.AhorroUseCase;
import com.finanzapp.domain.port.in.IngresoUseCase;
import com.finanzapp.domain.port.out.AbonoDeudaRepositoryPort;
import com.finanzapp.domain.port.out.DeudaRepositoryPort;
import com.finanzapp.domain.port.out.IngresoRepositoryPort;
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
public class IngresoService implements IngresoUseCase {

    private final IngresoRepositoryPort ingresoRepository;
    private final AhorroUseCase ahorroUseCase;
    private final DeudaRepositoryPort deudaRepository;
    private final AbonoDeudaRepositoryPort abonoDeudaRepository;

    @Override
    public Ingreso registrar(Ingreso ingreso) {
        ingreso.setId(UUID.randomUUID());
        ingreso.setFechaCreacion(LocalDateTime.now());
        ingreso.setFechaActualizacion(LocalDateTime.now());

        if (ingreso.getFecha() == null) {
            ingreso.setFecha(LocalDate.now());
        }

        if (ingreso.getMontoAhorro() == null) {
            ingreso.setMontoAhorro(BigDecimal.ZERO);
        }

        if (ingreso.getMetodoPago() == null) {
            ingreso.setMetodoPago(MetodoPago.EFECTIVO);
        }

        Ingreso registrado = ingresoRepository.save(ingreso);

        if (registrado.getMontoAhorro().compareTo(BigDecimal.ZERO) > 0) {
            Ahorro ahorro = Ahorro.builder()
                    .usuarioId(registrado.getUsuarioId())
                    .ingresoId(registrado.getId())
                    .metaId(ingreso.getMetaId())
                    .monto(registrado.getMontoAhorro())
                    .descripcion("Ahorro automático desde ingreso")
                    .fecha(registrado.getFecha())
                    .build();
            ahorroUseCase.registrar(ahorro);
        }

        if (ingreso.getPrestamoId() != null) {
            aplicarAbonoPrestamoDesdeIngreso(registrado, ingreso.getPrestamoId());
        }

        return registrado;
    }

    @Override
    @Transactional(readOnly = true)
    public Ingreso obtenerPorId(UUID id) {
        return ingresoRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Ingreso", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingreso> listarPorUsuario(UUID usuarioId) {
        return ingresoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingreso> listarPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return ingresoRepository.findByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingreso> listarPorCategoria(UUID usuarioId, CategoriaIngreso categoria) {
        return ingresoRepository.findByUsuarioIdAndCategoria(usuarioId, categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalIngresos(UUID usuarioId) {
        BigDecimal total = ingresoRepository.sumMontoByUsuarioId(usuarioId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalIngresosPorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        BigDecimal total = ingresoRepository.sumMontoByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Ingreso actualizar(UUID id, Ingreso ingresoActualizado) {
        Ingreso ingreso = obtenerPorId(id);
        BigDecimal montoAnterior = ingreso.getMonto();

        if (ingresoActualizado.getMonto() != null) {
            ingreso.setMonto(ingresoActualizado.getMonto());
        }
        if (ingresoActualizado.getCategoria() != null) {
            ingreso.setCategoria(ingresoActualizado.getCategoria());
        }
        if (ingresoActualizado.getCategoriaPersonalizadaId() != null) {
            ingreso.setCategoriaPersonalizadaId(ingresoActualizado.getCategoriaPersonalizadaId());
        }
        if (ingresoActualizado.getDescripcion() != null) {
            ingreso.setDescripcion(ingresoActualizado.getDescripcion());
        }
        if (ingresoActualizado.getFecha() != null) {
            ingreso.setFecha(ingresoActualizado.getFecha());
        }
        if (ingresoActualizado.getMontoAhorro() != null) {
            ingreso.setMontoAhorro(ingresoActualizado.getMontoAhorro());
        }
        if (ingresoActualizado.getMetodoPago() != null) {
            ingreso.setMetodoPago(ingresoActualizado.getMetodoPago());
        }

        ingreso.setFechaActualizacion(LocalDateTime.now());
        Ingreso guardado = ingresoRepository.save(ingreso);

        sincronizarAhorroDeIngreso(guardado, ingresoActualizado.getMetaId());
        sincronizarAbonoPrestamo(guardado, montoAnterior);

        return guardado;
    }

    @Override
    public void eliminar(UUID id) {
        obtenerPorId(id);
        ahorroUseCase.buscarPorIngresoId(id).ifPresent(ahorro -> ahorroUseCase.eliminar(ahorro.getId()));

        // Revertir el abono al prestamo si existe
        abonoDeudaRepository.findByIngresoId(id).ifPresent(abono -> {
            deudaRepository.findById(abono.getDeudaId()).ifPresent(deuda -> {
                deuda.setMontoAbonado(deuda.getMontoAbonado().subtract(abono.getMonto()).max(BigDecimal.ZERO));
                deuda.recalcularRestante();
                actualizarEstadoDeuda(deuda);
                deuda.setFechaActualizacion(LocalDateTime.now());
                deudaRepository.save(deuda);
            });
            abonoDeudaRepository.deleteById(abono.getId());
        });

        ingresoRepository.deleteById(id);
    }

    private void sincronizarAhorroDeIngreso(Ingreso ingreso, UUID metaId) {
        BigDecimal montoAhorro = ingreso.getMontoAhorro() != null ? ingreso.getMontoAhorro() : BigDecimal.ZERO;
        Optional<Ahorro> ahorroExistente = ahorroUseCase.buscarPorIngresoId(ingreso.getId());

        if (montoAhorro.compareTo(BigDecimal.ZERO) > 0) {
            if (ahorroExistente.isPresent()) {
                Ahorro ahorro = ahorroExistente.get();
                ahorro.setMonto(montoAhorro);
                if (metaId != null) {
                    ahorro.setMetaId(metaId);
                }
                ahorro.setFecha(ingreso.getFecha());
                ahorroUseCase.actualizar(ahorro.getId(), ahorro);
            } else {
                Ahorro nuevoAhorro = Ahorro.builder()
                        .usuarioId(ingreso.getUsuarioId())
                        .ingresoId(ingreso.getId())
                        .metaId(metaId)
                        .monto(montoAhorro)
                        .descripcion("Ahorro automático desde ingreso")
                        .fecha(ingreso.getFecha())
                        .build();
                ahorroUseCase.registrar(nuevoAhorro);
            }
        } else if (ahorroExistente.isPresent()) {
            ahorroUseCase.eliminar(ahorroExistente.get().getId());
        }
    }

    private void sincronizarAbonoPrestamo(Ingreso ingreso, BigDecimal montoAnterior) {
        Optional<AbonoDeuda> abonoExistente = abonoDeudaRepository.findByIngresoId(ingreso.getId());
        if (abonoExistente.isPresent() && !ingreso.getMonto().equals(montoAnterior)) {
            AbonoDeuda abono = abonoExistente.get();
            Deuda deuda = deudaRepository.findById(abono.getDeudaId())
                    .orElseThrow(() -> new RecursoNotFoundException("Deuda", abono.getDeudaId()));
            BigDecimal diferencia = ingreso.getMonto().subtract(montoAnterior);
            deuda.setMontoAbonado(deuda.getMontoAbonado().add(diferencia).max(BigDecimal.ZERO));
            deuda.recalcularRestante();
            actualizarEstadoDeuda(deuda);
            deuda.setFechaActualizacion(LocalDateTime.now());
            deudaRepository.save(deuda);

            abono.setMonto(ingreso.getMonto());
            abonoDeudaRepository.save(abono);
        }
    }

    private void aplicarAbonoPrestamoDesdeIngreso(Ingreso ingreso, UUID prestamoId) {
        Deuda deuda = deudaRepository.findById(prestamoId)
                .orElseThrow(() -> new RecursoNotFoundException("Prestamo", prestamoId));

        if (deuda.getTipo() != TipoDeuda.PRESTAMO) {
            throw new DomainException("Solo se puede asociar un ingreso a un prestamo, no a una deuda propia");
        }
        if (deuda.getEstado() == EstadoDeuda.COMPLETADA) {
            throw new DomainException("El prestamo ya está completado");
        }
        if (ingreso.getMonto().compareTo(deuda.getMontoRestante()) > 0) {
            throw new DomainException("El monto del ingreso excede el saldo pendiente del prestamo");
        }

        deuda.setMontoAbonado(deuda.getMontoAbonado().add(ingreso.getMonto()));
        deuda.recalcularRestante();
        actualizarEstadoDeuda(deuda);
        deuda.setFechaActualizacion(LocalDateTime.now());
        deudaRepository.save(deuda);

        AbonoDeuda abono = AbonoDeuda.builder()
                .id(UUID.randomUUID())
                .deudaId(prestamoId)
                .ingresoId(ingreso.getId())
                .monto(ingreso.getMonto())
                .descripcion(ingreso.getDescripcion() != null ? ingreso.getDescripcion() : "Cobro desde ingreso")
                .fechaAbono(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .build();
        abonoDeudaRepository.save(abono);
    }

    public Ingreso obtenerPorIdValidado(UUID id, UUID usuarioId) {
        Ingreso ingreso = obtenerPorId(id);
        validarPropiedad(ingreso.getUsuarioId(), usuarioId, "ingreso");
        return ingreso;
    }

    public Ingreso actualizarValidado(UUID id, Ingreso ingresoActualizado, UUID usuarioId) {
        Ingreso ingreso = obtenerPorId(id);
        validarPropiedad(ingreso.getUsuarioId(), usuarioId, "ingreso");
        return actualizar(id, ingresoActualizado);
    }

    public void eliminarValidado(UUID id, UUID usuarioId) {
        Ingreso ingreso = obtenerPorId(id);
        validarPropiedad(ingreso.getUsuarioId(), usuarioId, "ingreso");
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

