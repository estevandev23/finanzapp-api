package com.finanzapp.application.service;

import com.finanzapp.domain.exception.AccesoDenegadoException;
import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.Deuda;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.model.GastoMetodoPago;
import com.finanzapp.domain.model.Ingreso;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoDeuda;
import com.finanzapp.domain.model.TipoRecurrencia;
import com.finanzapp.domain.port.in.DeudaUseCase;
import com.finanzapp.domain.port.in.GastoUseCase;
import com.finanzapp.domain.port.in.IngresoUseCase;
import com.finanzapp.domain.port.in.RecurrenciaUseCase;
import com.finanzapp.domain.port.out.RecurrenciaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RecurrenciaService implements RecurrenciaUseCase {

    /** Días de anticipación con los que se permite confirmar una recurrencia antes de su vencimiento. */
    private static final int DIAS_ANTICIPACION_CONFIRMACION = 3;

    private final RecurrenciaRepositoryPort recurrenciaRepository;
    private final IngresoUseCase ingresoUseCase;
    private final GastoUseCase gastoUseCase;
    private final DeudaUseCase deudaUseCase;

    @Override
    public Recurrencia crear(Recurrencia recurrencia) {
        validar(recurrencia);

        recurrencia.setId(UUID.randomUUID());
        LocalDateTime ahora = LocalDateTime.now();
        recurrencia.setFechaCreacion(ahora);
        recurrencia.setFechaActualizacion(ahora);
        recurrencia.setActiva(true);

        if (recurrencia.getMetodoPago() == null) {
            recurrencia.setMetodoPago(MetodoPago.EFECTIVO);
        }
        validarCoherenciaTarjeta(recurrencia);
        if (recurrencia.getTipo() == TipoRecurrencia.INGRESO) {
            // Un bolsillo de presupuesto solo aplica a gastos recurrentes.
            recurrencia.setBolsilloId(null);
        }
        if (recurrencia.getProximaFecha() == null) {
            recurrencia.setProximaFecha(calcularPrimeraFecha(recurrencia));
        }

        return recurrenciaRepository.save(recurrencia);
    }

    @Override
    @Transactional(readOnly = true)
    public Recurrencia obtenerPorId(UUID id) {
        return recurrenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Recurrencia", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recurrencia> listarPorUsuario(UUID usuarioId) {
        return recurrenciaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recurrencia> listarPorTipo(UUID usuarioId, TipoRecurrencia tipo) {
        return recurrenciaRepository.findByUsuarioIdAndTipo(usuarioId, tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recurrencia> listarActivasPorUsuario(UUID usuarioId) {
        return recurrenciaRepository.findActivasByUsuarioId(usuarioId);
    }

    @Override
    public Recurrencia actualizar(UUID id, Recurrencia datos) {
        Recurrencia actual = obtenerPorId(id);

        if (datos.getDescripcion() != null) actual.setDescripcion(datos.getDescripcion());
        if (datos.getMonto() != null) actual.setMonto(datos.getMonto());
        if (datos.getFrecuencia() != null) actual.setFrecuencia(datos.getFrecuencia());
        if (datos.getCategoriaIngreso() != null) actual.setCategoriaIngreso(datos.getCategoriaIngreso());
        if (datos.getCategoriaGasto() != null) actual.setCategoriaGasto(datos.getCategoriaGasto());
        if (datos.getCategoriaPersonalizadaId() != null) {
            actual.setCategoriaPersonalizadaId(datos.getCategoriaPersonalizadaId());
        }
        if (datos.getMetodoPago() != null) actual.setMetodoPago(datos.getMetodoPago());
        if (datos.getTarjetaId() != null) actual.setTarjetaId(datos.getTarjetaId());
        if (datos.getBolsilloId() != null) actual.setBolsilloId(datos.getBolsilloId());
        if (datos.getDiaVencimiento() > 0) actual.setDiaVencimiento(datos.getDiaVencimiento());
        if (datos.getMesReferencia() != null) actual.setMesReferencia(datos.getMesReferencia());
        if (datos.getProximaFecha() != null) actual.setProximaFecha(datos.getProximaFecha());

        actual.setFechaActualizacion(LocalDateTime.now());
        validarCoherenciaTarjeta(actual);
        return recurrenciaRepository.save(actual);
    }

    @Override
    public Recurrencia cambiarEstado(UUID id, boolean activa) {
        Recurrencia actual = obtenerPorId(id);
        actual.setActiva(activa);
        actual.setFechaActualizacion(LocalDateTime.now());
        return recurrenciaRepository.save(actual);
    }

    @Override
    public void eliminar(UUID id) {
        obtenerPorId(id);
        recurrenciaRepository.deleteById(id);
    }

    @Override
    public UUID confirmar(UUID recurrenciaId, LocalDate fechaConfirmacion) {
        Recurrencia recurrencia = obtenerPorId(recurrenciaId);
        if (!recurrencia.isActiva()) {
            throw new DomainException("La recurrencia está inactiva. Actívala antes de confirmarla.");
        }

        // Idempotencia: solo se puede confirmar una recurrencia que esté vencida o por vencer
        // dentro de la ventana de anticipación. Tras confirmar, la próxima fecha avanza un periodo
        // completo (>= 15 días), por lo que una segunda confirmación inmediata queda bloqueada y se
        // evita generar el mismo movimiento dos veces.
        LocalDate hoy = LocalDate.now();
        if (recurrencia.getProximaFecha() != null
                && recurrencia.getProximaFecha().isAfter(hoy.plusDays(DIAS_ANTICIPACION_CONFIRMACION))) {
            throw new DomainException("La recurrencia aún no está disponible para confirmar; su próxima fecha es "
                    + recurrencia.getProximaFecha() + ".");
        }

        LocalDate fecha = fechaConfirmacion != null ? fechaConfirmacion : LocalDate.now();
        UUID idGenerado = generarRegistroDesdeRecurrencia(recurrencia, fecha);

        recurrencia.setUltimaConfirmacionFecha(fecha);
        recurrencia.avanzarProximaFecha();
        recurrencia.setFechaActualizacion(LocalDateTime.now());
        recurrenciaRepository.save(recurrencia);

        return idGenerado;
    }

    public Recurrencia obtenerPorIdValidado(UUID id, UUID usuarioId) {
        Recurrencia recurrencia = obtenerPorId(id);
        validarPropiedad(recurrencia.getUsuarioId(), usuarioId);
        return recurrencia;
    }

    public void eliminarValidado(UUID id, UUID usuarioId) {
        obtenerPorIdValidado(id, usuarioId);
        recurrenciaRepository.deleteById(id);
    }

    public Recurrencia actualizarValidado(UUID id, Recurrencia datos, UUID usuarioId) {
        obtenerPorIdValidado(id, usuarioId);
        return actualizar(id, datos);
    }

    public Recurrencia cambiarEstadoValidado(UUID id, boolean activa, UUID usuarioId) {
        obtenerPorIdValidado(id, usuarioId);
        return cambiarEstado(id, activa);
    }

    public UUID confirmarValidado(UUID id, LocalDate fechaConfirmacion, UUID usuarioId) {
        obtenerPorIdValidado(id, usuarioId);
        return confirmar(id, fechaConfirmacion);
    }

    private UUID generarRegistroDesdeRecurrencia(Recurrencia recurrencia, LocalDate fecha) {
        if (recurrencia.getTipo() == TipoRecurrencia.INGRESO) {
            Ingreso ingreso = Ingreso.builder()
                    .usuarioId(recurrencia.getUsuarioId())
                    .monto(recurrencia.getMonto())
                    .categoria(recurrencia.getCategoriaIngreso())
                    .categoriaPersonalizadaId(recurrencia.getCategoriaPersonalizadaId())
                    .descripcion(recurrencia.getDescripcion())
                    .fecha(fecha)
                    .metodoPago(recurrencia.getMetodoPago())
                    .build();
            return ingresoUseCase.registrar(ingreso).getId();
        }

        boolean esTarjetaCredito = recurrencia.getMetodoPago() == MetodoPago.TARJETA_CREDITO
                && recurrencia.getTarjetaId() != null;

        Gasto gasto = Gasto.builder()
                .usuarioId(recurrencia.getUsuarioId())
                .monto(recurrencia.getMonto())
                .categoria(recurrencia.getCategoriaGasto())
                .categoriaPersonalizadaId(recurrencia.getCategoriaPersonalizadaId())
                .descripcion(recurrencia.getDescripcion())
                .fecha(fecha)
                .tarjetaId(recurrencia.getTarjetaId())
                .bolsilloId(recurrencia.getBolsilloId())
                .metodosPago(List.of(GastoMetodoPago.builder()
                        .metodo(recurrencia.getMetodoPago())
                        .monto(recurrencia.getMonto())
                        .build()))
                .build();

        if (!esTarjetaCredito) {
            return gastoUseCase.registrar(gasto).getId();
        }

        // Compra con tarjeta de crédito: el gasto registra el consumo (para presupuesto y
        // reportes) sin afectar el cupo, porque el cupo lo gestiona la deuda generada. Así se
        // evita contar el mismo movimiento dos veces contra la tarjeta.
        Gasto gastoGenerado = gastoUseCase.registrarSinAfectarCupo(gasto);
        generarDeudaPorTarjeta(recurrencia, fecha);
        return gastoGenerado.getId();
    }

    /**
     * Genera una deuda independiente por cada compra con tarjeta de crédito. El saldo de la
     * deuda consume cupo de la tarjeta y se libera al abonarla, momento en el que el dinero
     * realmente sale del disponible del usuario.
     */
    private void generarDeudaPorTarjeta(Recurrencia recurrencia, LocalDate fecha) {
        Deuda deuda = Deuda.builder()
                .usuarioId(recurrencia.getUsuarioId())
                .tipo(TipoDeuda.DEUDA)
                .descripcion(recurrencia.getDescripcion())
                .entidad(recurrencia.getTarjetaNombre())
                .categoria(recurrencia.getCategoriaGasto() != null
                        ? recurrencia.getCategoriaGasto().name() : null)
                .categoriaPersonalizadaId(recurrencia.getCategoriaPersonalizadaId())
                .montoTotal(recurrencia.getMonto())
                .tarjetaId(recurrencia.getTarjetaId())
                .fechaInicio(fecha)
                .build();
        deudaUseCase.registrar(deuda);
    }

    private void validar(Recurrencia recurrencia) {
        if (recurrencia.getUsuarioId() == null) {
            throw new DomainException("usuarioId es requerido");
        }
        if (recurrencia.getTipo() == null) {
            throw new DomainException("tipo es requerido (INGRESO o GASTO)");
        }
        if (recurrencia.getFrecuencia() == null) {
            throw new DomainException("frecuencia es requerida");
        }
        if (recurrencia.getMonto() == null || recurrencia.getMonto().signum() <= 0) {
            throw new DomainException("monto debe ser mayor a 0");
        }
        if (recurrencia.getDescripcion() == null || recurrencia.getDescripcion().isBlank()) {
            throw new DomainException("descripcion es requerida");
        }
        if (recurrencia.getDiaVencimiento() < 1 || recurrencia.getDiaVencimiento() > 31) {
            throw new DomainException("diaVencimiento debe estar entre 1 y 31");
        }
        if (recurrencia.getMesReferencia() != null
                && (recurrencia.getMesReferencia() < 1 || recurrencia.getMesReferencia() > 12)) {
            throw new DomainException("mesReferencia debe estar entre 1 y 12");
        }
        if (recurrencia.getTipo() == TipoRecurrencia.INGRESO
                && recurrencia.getCategoriaGasto() != null) {
            throw new DomainException("Una recurrencia de tipo INGRESO no puede tener categoriaGasto");
        }
        if (recurrencia.getTipo() == TipoRecurrencia.GASTO
                && recurrencia.getCategoriaIngreso() != null) {
            throw new DomainException("Una recurrencia de tipo GASTO no puede tener categoriaIngreso");
        }
    }

    private LocalDate calcularPrimeraFecha(Recurrencia recurrencia) {
        LocalDate hoy = LocalDate.now();
        int dia = Math.min(recurrencia.getDiaVencimiento(), hoy.lengthOfMonth());

        return switch (recurrencia.getFrecuencia()) {
            case MENSUAL -> {
                LocalDate candidata = hoy.withDayOfMonth(dia);
                yield candidata.isBefore(hoy) ? candidata.plusMonths(1)
                        .withDayOfMonth(Math.min(recurrencia.getDiaVencimiento(),
                                candidata.plusMonths(1).lengthOfMonth())) : candidata;
            }
            case QUINCENAL -> hoy.plusDays(15);
            case SEMESTRAL, ANUAL -> {
                int mesObjetivo = recurrencia.getMesReferencia() != null
                        ? recurrencia.getMesReferencia()
                        : hoy.getMonthValue();
                LocalDate candidata = LocalDate.of(hoy.getYear(), mesObjetivo, 1)
                        .withDayOfMonth(Math.min(recurrencia.getDiaVencimiento(),
                                LocalDate.of(hoy.getYear(), mesObjetivo, 1).lengthOfMonth()));
                if (candidata.isBefore(hoy)) {
                    candidata = recurrencia.getFrecuencia().calcularSiguiente(candidata, recurrencia.getDiaVencimiento());
                }
                yield candidata;
            }
        };
    }

    private void validarPropiedad(UUID propietarioId, UUID solicitanteId) {
        if (!propietarioId.equals(solicitanteId)) {
            throw new AccesoDenegadoException("recurrencia");
        }
    }

    private void validarCoherenciaTarjeta(Recurrencia recurrencia) {
        boolean usaTarjeta = recurrencia.getMetodoPago() == MetodoPago.TARJETA_CREDITO;
        if (usaTarjeta && recurrencia.getTarjetaId() == null) {
            throw new DomainException("Cuando el método de pago es TARJETA_CREDITO debe indicarse la tarjeta (tarjetaId).");
        }
        if (!usaTarjeta) {
            recurrencia.setTarjetaId(null);
        }
    }
}
