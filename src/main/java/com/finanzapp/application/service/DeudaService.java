package com.finanzapp.application.service;

import com.finanzapp.domain.exception.AccesoDenegadoException;
import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.model.AbonoDeuda;
import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.CategoriaIngreso;
import com.finanzapp.domain.model.Deuda;
import com.finanzapp.domain.model.EstadoDeuda;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.model.GastoMetodoPago;
import com.finanzapp.domain.model.Ingreso;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.TipoDeuda;
import com.finanzapp.domain.port.in.DeudaUseCase;
import com.finanzapp.domain.port.in.TarjetaCreditoUseCase;
import com.finanzapp.domain.port.out.AbonoDeudaRepositoryPort;
import com.finanzapp.domain.port.out.DeudaRepositoryPort;
import com.finanzapp.domain.port.out.GastoRepositoryPort;
import com.finanzapp.domain.port.out.IngresoRepositoryPort;
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
public class DeudaService implements DeudaUseCase {

    private final DeudaRepositoryPort deudaRepository;
    private final AbonoDeudaRepositoryPort abonoRepository;
    private final GastoRepositoryPort gastoRepository;
    private final IngresoRepositoryPort ingresoRepository;
    private final TarjetaCreditoUseCase tarjetaUseCase;

    @Override
    public Deuda registrar(Deuda deuda) {
        deuda.setId(UUID.randomUUID());
        deuda.setMontoAbonado(BigDecimal.ZERO);
        deuda.setMontoRestante(deuda.getMontoTotal());
        deuda.setEstado(EstadoDeuda.PENDIENTE);
        deuda.setFechaCreacion(LocalDateTime.now());
        deuda.setFechaActualizacion(LocalDateTime.now());

        aplicarReglasTarjeta(deuda);

        Deuda guardada = deudaRepository.save(deuda);

        AbonoDeuda abonoInicial = AbonoDeuda.builder()
                .id(UUID.randomUUID())
                .deudaId(guardada.getId())
                .monto(BigDecimal.ZERO)
                .descripcion("Creacion de " + (deuda.getTipo() == TipoDeuda.DEUDA ? "deuda" : "prestamo"))
                .fechaAbono(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .build();
        abonoRepository.save(abonoInicial);

        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Deuda obtenerPorId(UUID id) {
        return deudaRepository.findById(id)
                .orElseThrow(() -> new DomainException("Deuda no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deuda> listarPorUsuario(UUID usuarioId) {
        return deudaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deuda> listarPorTipo(UUID usuarioId, TipoDeuda tipo) {
        return deudaRepository.findByUsuarioIdAndTipo(usuarioId, tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deuda> listarPorEstado(UUID usuarioId, EstadoDeuda estado) {
        return deudaRepository.findByUsuarioIdAndEstado(usuarioId, estado);
    }

    @Override
    public Deuda actualizar(UUID id, Deuda datosActualizados) {
        Deuda existente = obtenerPorId(id);

        UUID tarjetaAnterior = existente.getTarjetaId();
        BigDecimal restanteAnterior = existente.getMontoRestante() != null
                ? existente.getMontoRestante()
                : BigDecimal.ZERO;

        if (datosActualizados.getDescripcion() != null) {
            existente.setDescripcion(datosActualizados.getDescripcion());
        }
        if (datosActualizados.getEntidad() != null) {
            existente.setEntidad(datosActualizados.getEntidad());
        }
        if (datosActualizados.getCategoria() != null) {
            existente.setCategoria(datosActualizados.getCategoria());
        }
        existente.setCategoriaPersonalizadaId(datosActualizados.getCategoriaPersonalizadaId());
        if (datosActualizados.getFechaLimite() != null) {
            existente.setFechaLimite(datosActualizados.getFechaLimite());
        }
        if (datosActualizados.getMontoTotal() != null) {
            BigDecimal nuevoTotal = datosActualizados.getMontoTotal();
            if (nuevoTotal.compareTo(existente.getMontoAbonado()) < 0) {
                throw new DomainException("El monto total no puede ser menor al monto ya abonado");
            }
            existente.setMontoTotal(nuevoTotal);
            existente.recalcularRestante();
            verificarCompletada(existente);

            AbonoDeuda auditoria = AbonoDeuda.builder()
                    .id(UUID.randomUUID())
                    .deudaId(id)
                    .monto(BigDecimal.ZERO)
                    .descripcion("Actualizacion del monto total a " + nuevoTotal)
                    .fechaAbono(LocalDateTime.now())
                    .fechaCreacion(LocalDateTime.now())
                    .build();
            abonoRepository.save(auditoria);
        }

        // Solo aplica a deudas (no a prestamos): la asociacion con tarjeta de credito
        // ocupa cupo. Si el tipo no es DEUDA, se fuerza tarjetaId=null.
        UUID tarjetaNueva = existente.getTipo() == TipoDeuda.DEUDA
                ? datosActualizados.getTarjetaId()
                : null;
        existente.setTarjetaId(tarjetaNueva);
        BigDecimal restanteNuevo = existente.getMontoRestante() != null
                ? existente.getMontoRestante()
                : BigDecimal.ZERO;

        sincronizarCupoEnActualizacion(existente, tarjetaAnterior, restanteAnterior, tarjetaNueva, restanteNuevo);

        existente.setFechaActualizacion(LocalDateTime.now());
        return deudaRepository.save(existente);
    }

    /**
     * Ajusta el cupo usado de las tarjetas cuando se actualiza una deuda. Cubre los casos
     * de asignar, cambiar o desasociar una tarjeta y los cambios de monto cuando la deuda
     * mantiene una tarjeta asociada.
     */
    private void sincronizarCupoEnActualizacion(Deuda deuda,
                                                UUID tarjetaAnterior,
                                                BigDecimal restanteAnterior,
                                                UUID tarjetaNueva,
                                                BigDecimal restanteNuevo) {
        if (deuda.getTipo() != TipoDeuda.DEUDA) return;

        boolean cambioTarjeta = !java.util.Objects.equals(tarjetaAnterior, tarjetaNueva);

        if (cambioTarjeta) {
            if (tarjetaAnterior != null && restanteAnterior.signum() > 0) {
                tarjetaUseCase.disminuirCupoUsado(tarjetaAnterior, restanteAnterior);
            }
            if (tarjetaNueva != null && restanteNuevo.signum() > 0) {
                validarTarjetaPropia(tarjetaNueva, deuda.getUsuarioId());
                tarjetaUseCase.aumentarCupoUsado(tarjetaNueva, restanteNuevo);
            }
            return;
        }

        if (tarjetaNueva == null) return;

        BigDecimal delta = restanteNuevo.subtract(restanteAnterior);
        if (delta.signum() > 0) {
            tarjetaUseCase.aumentarCupoUsado(tarjetaNueva, delta);
        } else if (delta.signum() < 0) {
            tarjetaUseCase.disminuirCupoUsado(tarjetaNueva, delta.abs());
        }
    }

    private void validarTarjetaPropia(UUID tarjetaId, UUID usuarioId) {
        com.finanzapp.domain.model.TarjetaCredito tarjeta = tarjetaUseCase.obtenerPorId(tarjetaId);
        if (!tarjeta.getUsuarioId().equals(usuarioId)) {
            throw new DomainException("La tarjeta indicada no pertenece al usuario.");
        }
    }

    @Override
    public void eliminar(UUID id) {
        Deuda deuda = obtenerPorId(id);
        if (deuda.getTipo() == TipoDeuda.DEUDA && deuda.getTarjetaId() != null
                && deuda.getMontoRestante() != null && deuda.getMontoRestante().signum() > 0) {
            // Libera del cupo de la tarjeta el saldo pendiente al eliminar la deuda.
            tarjetaUseCase.disminuirCupoUsado(deuda.getTarjetaId(), deuda.getMontoRestante());
        }
        // Eliminar en cascada respetando las FK: abonos → gastos → ingresos → deuda
        abonoRepository.deleteAllByDeudaId(id);
        gastoRepository.deleteAllByDeudaId(id);
        ingresoRepository.deleteAllByPrestamoId(id);
        deudaRepository.deleteById(id);
    }

    @Override
    public AbonoDeuda registrarAbono(UUID deudaId, BigDecimal monto, String descripcion, MetodoPago metodoPago) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("El monto del abono debe ser mayor a 0");
        }

        Deuda deuda = obtenerPorId(deudaId);

        if (deuda.getEstado() == EstadoDeuda.COMPLETADA) {
            throw new DomainException("La deuda ya esta completada, no se pueden registrar mas abonos");
        }

        if (monto.compareTo(deuda.getMontoRestante()) > 0) {
            throw new DomainException("El monto del abono excede el saldo restante de la deuda");
        }

        deuda.setMontoAbonado(deuda.getMontoAbonado().add(monto));
        deuda.recalcularRestante();
        verificarCompletada(deuda);
        deuda.setFechaActualizacion(LocalDateTime.now());
        deudaRepository.save(deuda);

        AbonoDeuda abono = AbonoDeuda.builder()
                .id(UUID.randomUUID())
                .deudaId(deudaId)
                .monto(monto)
                .descripcion(descripcion)
                .fechaAbono(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .build();

        AbonoDeuda guardado = abonoRepository.save(abono);

        if (deuda.getTipo() == TipoDeuda.DEUDA) {
            guardado = crearGastoParaAbono(guardado, deuda, metodoPago);
            if (deuda.getTarjetaId() != null) {
                // Libera cupo de la tarjeta: el abono salda parte del saldo en tarjeta.
                tarjetaUseCase.disminuirCupoUsado(deuda.getTarjetaId(), monto);
            }
        } else {
            guardado = crearIngresoParaAbono(guardado, deuda, metodoPago);
        }

        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbonoDeuda> listarAbonos(UUID deudaId) {
        return abonoRepository.findByDeudaId(deudaId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalDeudas(UUID usuarioId) {
        BigDecimal total = deudaRepository.sumMontoRestanteByUsuarioIdAndTipo(usuarioId, TipoDeuda.DEUDA);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalPrestamos(UUID usuarioId) {
        BigDecimal total = deudaRepository.sumMontoRestanteByUsuarioIdAndTipo(usuarioId, TipoDeuda.PRESTAMO);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalAbonosPrestamosRecibidos(UUID usuarioId) {
        BigDecimal total = deudaRepository.sumMontoAbonadoByUsuarioIdAndTipo(usuarioId, TipoDeuda.PRESTAMO);
        return total != null ? total : BigDecimal.ZERO;
    }

    private AbonoDeuda crearGastoParaAbono(AbonoDeuda abono, Deuda deuda, MetodoPago metodoPago) {
        String descripcionGasto = abono.getDescripcion() != null
                ? abono.getDescripcion()
                : "Abono a " + (deuda.getEntidad() != null ? deuda.getEntidad() : "deuda");

        CategoriaGasto categoriaGasto = CategoriaGasto.ABONO;
        if (deuda.getCategoria() != null) {
            try {
                categoriaGasto = CategoriaGasto.valueOf(deuda.getCategoria());
            } catch (IllegalArgumentException ignored) {
            }
        }

        GastoMetodoPago gastoMetodoPago = GastoMetodoPago.builder()
                .id(UUID.randomUUID())
                .metodo(metodoPago)
                .monto(abono.getMonto())
                .build();

        LocalDate fechaGasto = LocalDate.now();
        Gasto gasto = Gasto.builder()
                .id(UUID.randomUUID())
                .usuarioId(deuda.getUsuarioId())
                .monto(abono.getMonto())
                .categoria(categoriaGasto)
                .categoriaPersonalizadaId(deuda.getCategoriaPersonalizadaId())
                .deudaId(deuda.getId())
                .descripcion(descripcionGasto)
                .metodosPago(List.of(gastoMetodoPago))
                .fecha(fechaGasto)
                .mesFacturacion(fechaGasto.withDayOfMonth(1))
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Gasto gastoGuardado = gastoRepository.save(gasto);

        abono.setGastoId(gastoGuardado.getId());
        return abonoRepository.save(abono);
    }

    private AbonoDeuda crearIngresoParaAbono(AbonoDeuda abono, Deuda deuda, MetodoPago metodoPago) {
        String descripcionIngreso = abono.getDescripcion() != null
                ? abono.getDescripcion()
                : "Cobro de " + (deuda.getEntidad() != null ? deuda.getEntidad() : "prestamo");

        Ingreso ingreso = Ingreso.builder()
                .id(UUID.randomUUID())
                .usuarioId(deuda.getUsuarioId())
                .monto(abono.getMonto())
                .categoria(CategoriaIngreso.ABONO)
                .metodoPago(metodoPago)
                .prestamoId(deuda.getId())
                .montoAhorro(BigDecimal.ZERO)
                .descripcion(descripcionIngreso)
                .fecha(LocalDate.now())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Ingreso ingresoGuardado = ingresoRepository.save(ingreso);

        abono.setIngresoId(ingresoGuardado.getId());
        return abonoRepository.save(abono);
    }

    public Deuda obtenerPorIdValidado(UUID id, UUID usuarioId) {
        Deuda deuda = obtenerPorId(id);
        validarPropiedad(deuda.getUsuarioId(), usuarioId, "deuda");
        return deuda;
    }

    public Deuda actualizarValidado(UUID id, Deuda datosActualizados, UUID usuarioId) {
        Deuda deuda = obtenerPorId(id);
        validarPropiedad(deuda.getUsuarioId(), usuarioId, "deuda");
        return actualizar(id, datosActualizados);
    }

    public void eliminarValidado(UUID id, UUID usuarioId) {
        Deuda deuda = obtenerPorId(id);
        validarPropiedad(deuda.getUsuarioId(), usuarioId, "deuda");
        eliminar(id);
    }

    private void validarPropiedad(UUID propietarioId, UUID solicitanteId, String recurso) {
        if (!propietarioId.equals(solicitanteId)) {
            throw new AccesoDenegadoException(recurso);
        }
    }

    private void verificarCompletada(Deuda deuda) {
        if (deuda.isCompletada()) {
            deuda.setEstado(EstadoDeuda.COMPLETADA);
        } else if (deuda.getMontoAbonado().compareTo(BigDecimal.ZERO) > 0) {
            deuda.setEstado(EstadoDeuda.EN_CURSO);
        }
    }

    /**
     * Si la deuda está asociada a una tarjeta de crédito (compra a crédito), aumenta el
     * cupo usado por el saldo restante. Solo aplica para tipo DEUDA (no para PRESTAMO).
     */
    private void aplicarReglasTarjeta(Deuda deuda) {
        if (deuda.getTarjetaId() == null) return;
        if (deuda.getTipo() != TipoDeuda.DEUDA) {
            // Un prestamo (dinero que me deben) no aplica contra el cupo de mi tarjeta.
            deuda.setTarjetaId(null);
            return;
        }
        com.finanzapp.domain.model.TarjetaCredito tarjeta = tarjetaUseCase.obtenerPorId(deuda.getTarjetaId());
        if (!tarjeta.getUsuarioId().equals(deuda.getUsuarioId())) {
            throw new DomainException("La tarjeta indicada no pertenece al usuario.");
        }
        tarjetaUseCase.aumentarCupoUsado(tarjeta.getId(), deuda.getMontoTotal());
    }
}