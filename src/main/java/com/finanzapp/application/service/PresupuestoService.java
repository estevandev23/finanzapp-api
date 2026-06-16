package com.finanzapp.application.service;

import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.Bolsillo;
import com.finanzapp.domain.model.BolsilloMensual;
import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.EstadoBolsilloMensual;
import com.finanzapp.domain.model.EstadoPresupuestoMensual;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.model.MovimientoBolsillo;
import com.finanzapp.domain.model.PresupuestoMensual;
import com.finanzapp.domain.model.PresupuestoPlantilla;
import com.finanzapp.domain.model.PreviewGastoBolsillo;
import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoBasePresupuesto;
import com.finanzapp.domain.model.TipoRecurrencia;
import com.finanzapp.domain.port.in.PresupuestoUseCase;
import com.finanzapp.domain.port.out.GastoRepositoryPort;
import com.finanzapp.domain.port.out.IngresoRepositoryPort;
import com.finanzapp.domain.port.out.PresupuestoRepositoryPort;
import com.finanzapp.domain.port.out.RecurrenciaRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PresupuestoService implements PresupuestoUseCase {

    private final PresupuestoRepositoryPort presupuestoRepository;
    private final GastoRepositoryPort gastoRepository;
    private final IngresoRepositoryPort ingresoRepository;
    private final RecurrenciaRepositoryPort recurrenciaRepository;

    @Override
    public PresupuestoPlantilla guardarPlantilla(UUID usuarioId, PresupuestoPlantilla plantilla) {
        validarPlantilla(plantilla);

        LocalDateTime ahora = LocalDateTime.now();
        Optional<PresupuestoPlantilla> existente = presupuestoRepository.findPlantillaByUsuarioId(usuarioId);

        // Reutiliza el ID y la fecha de creacion para que el save sea UPDATE.
        // Hacer delete+insert en la misma transaccion violaria UNIQUE(usuario_id)
        // porque Hibernate reordena las operaciones (insert antes del delete).
        plantilla.setId(existente.map(PresupuestoPlantilla::getId).orElseGet(UUID::randomUUID));
        plantilla.setUsuarioId(usuarioId);
        plantilla.setFechaCreacion(existente.map(PresupuestoPlantilla::getFechaCreacion).orElse(ahora));
        plantilla.setFechaActualizacion(ahora);

        if (plantilla.getBolsillos() != null) {
            // Preserva los IDs de bolsillos ya existentes para no romper las asociaciones
            // (gastos.bolsillo_id y recurrencias.bolsillo_id tienen ON DELETE SET NULL).
            // Solo se aceptan IDs que pertenezcan a la plantilla del propio usuario.
            Set<UUID> idsExistentes = existente
                    .map(p -> p.getBolsillos().stream().map(Bolsillo::getId).collect(Collectors.toSet()))
                    .orElseGet(Set::of);

            int orden = 0;
            for (Bolsillo b : plantilla.getBolsillos()) {
                boolean conservaId = b.getId() != null && idsExistentes.contains(b.getId());
                if (!conservaId) {
                    b.setId(UUID.randomUUID());
                }
                b.setPlantillaId(plantilla.getId());
                if (b.getOrden() == null) b.setOrden(orden);
                orden++;
            }
        }

        PresupuestoPlantilla saved = presupuestoRepository.savePlantilla(plantilla);

        // Sincroniza el snapshot del mes actual de inmediato (en lugar de borrarlo):
        // así los bolsilloMensualId se conservan y el estado refleja los cambios
        // sin ventanas de inconsistencia ni referencias rotas en el frontend.
        YearMonth mesActual = YearMonth.now();
        regenerarMensual(usuarioId, mesActual.getYear(), mesActual.getMonthValue());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PresupuestoPlantilla> obtenerPlantilla(UUID usuarioId) {
        return presupuestoRepository.findPlantillaByUsuarioId(usuarioId);
    }

    @Override
    public EstadoPresupuestoMensual obtenerEstadoMensual(UUID usuarioId, int anio, int mes) {
        PresupuestoMensual mensual = presupuestoRepository
                .findMensualByUsuarioAnioMes(usuarioId, anio, mes)
                .orElseGet(() -> regenerarMensual(usuarioId, anio, mes));

        return calcularEstado(usuarioId, mensual);
    }

    @Override
    public PresupuestoMensual regenerarMensual(UUID usuarioId, int anio, int mes) {
        PresupuestoPlantilla plantilla = presupuestoRepository.findPlantillaByUsuarioId(usuarioId)
                .orElseThrow(() -> new DomainException(
                        "El usuario aún no tiene una plantilla de presupuesto configurada."));

        BigDecimal base = calcularBase(plantilla, usuarioId, anio, mes);

        // Reconcilia el snapshot existente en lugar de borrarlo y recrearlo: un
        // delete+insert en la misma transacción viola UNIQUE(usuario_id, anio, mes)
        // porque Hibernate ejecuta el insert antes del delete. Además, conservar los
        // BolsilloMensual existentes (matcheados por bolsilloOrigenId) mantiene
        // estables los IDs que el frontend y las alertas ya referencian.
        PresupuestoMensual mensual = presupuestoRepository
                .findMensualByUsuarioAnioMes(usuarioId, anio, mes)
                .orElseGet(() -> PresupuestoMensual.builder()
                        .id(UUID.randomUUID())
                        .usuarioId(usuarioId)
                        .anio(anio)
                        .mes(mes)
                        .bolsillos(new ArrayList<>())
                        .build());

        mensual.setBaseCalculada(base);
        mensual.setFechaCalculo(LocalDateTime.now());

        Map<UUID, BolsilloMensual> existentesPorOrigen = new HashMap<>();
        for (BolsilloMensual bm : mensual.getBolsillos()) {
            if (bm.getBolsilloOrigenId() != null) {
                existentesPorOrigen.put(bm.getBolsilloOrigenId(), bm);
            }
        }

        List<BolsilloMensual> reconciliados = new ArrayList<>();
        if (plantilla.getBolsillos() != null) {
            for (Bolsillo b : plantilla.getBolsillos()) {
                BigDecimal monto = base.multiply(b.getPorcentaje())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BolsilloMensual bm = existentesPorOrigen.get(b.getId());
                if (bm == null) {
                    bm = BolsilloMensual.builder()
                            .id(UUID.randomUUID())
                            .presupuestoMensualId(mensual.getId())
                            .bolsilloOrigenId(b.getId())
                            .build();
                }
                bm.setNombre(b.getNombre());
                bm.setTipo(b.getTipo());
                bm.setPorcentaje(b.getPorcentaje());
                bm.setMontoLimite(monto);
                bm.setColor(b.getColor());
                bm.setOrden(b.getOrden() != null ? b.getOrden() : reconciliados.size());
                reconciliados.add(bm);
            }
        }
        // Los bolsillos mensuales sin contraparte en la plantilla se eliminan por orphanRemoval.
        mensual.setBolsillos(reconciliados);

        return presupuestoRepository.saveMensual(mensual);
    }

    @Override
    public BolsilloMensual actualizarBolsilloMensual(UUID usuarioId, UUID bolsilloMensualId,
                                                     BigDecimal nuevoPorcentaje, BigDecimal nuevoMontoLimite) {
        BolsilloMensual bolsillo = presupuestoRepository.findBolsilloMensualById(bolsilloMensualId)
                .orElseThrow(() -> new RecursoNotFoundException("BolsilloMensual", bolsilloMensualId));

        PresupuestoMensual mensual = presupuestoRepository.findMensualById(bolsillo.getPresupuestoMensualId())
                .orElseThrow(() -> new RecursoNotFoundException(
                        "PresupuestoMensual", bolsillo.getPresupuestoMensualId()));

        if (!mensual.getUsuarioId().equals(usuarioId)) {
            throw new DomainException("El bolsillo no pertenece al usuario.");
        }

        if (nuevoPorcentaje != null) {
            if (nuevoPorcentaje.compareTo(BigDecimal.ZERO) <= 0
                    || nuevoPorcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new DomainException("El porcentaje debe estar entre 0 y 100.");
            }
            bolsillo.setPorcentaje(nuevoPorcentaje);
            BigDecimal nuevoMonto = mensual.getBaseCalculada().multiply(nuevoPorcentaje)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            bolsillo.setMontoLimite(nuevoMonto);
        }

        if (nuevoMontoLimite != null) {
            if (nuevoMontoLimite.compareTo(BigDecimal.ZERO) <= 0) {
                throw new DomainException("El monto límite debe ser mayor a cero.");
            }
            bolsillo.setMontoLimite(nuevoMontoLimite);
        }

        return presupuestoRepository.saveBolsilloMensual(bolsillo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoMensual> listarMensuales(UUID usuarioId) {
        return presupuestoRepository.findMensualesByUsuarioId(usuarioId);
    }

    @Override
    public PreviewGastoBolsillo previsualizarGasto(UUID usuarioId, UUID bolsilloOrigenId,
                                                   BigDecimal monto, int anio, int mes) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("El monto a previsualizar no puede ser negativo.");
        }
        BigDecimal nuevoMonto = monto != null ? monto : BigDecimal.ZERO;

        EstadoPresupuestoMensual estado = obtenerEstadoMensual(usuarioId, anio, mes);
        Optional<EstadoBolsilloMensual> actualOpt = buscarPorOrigen(estado, bolsilloOrigenId);

        if (actualOpt.isEmpty()) {
            // El snapshot puede estar desincronizado con la plantilla (p. ej. un bolsillo
            // recién agregado o datos previos a la estabilización de IDs). Se reconcilia
            // una vez y se reintenta antes de reportar el recurso como inexistente.
            regenerarMensual(usuarioId, anio, mes);
            estado = obtenerEstadoMensual(usuarioId, anio, mes);
            actualOpt = buscarPorOrigen(estado, bolsilloOrigenId);
        }

        EstadoBolsilloMensual actual = actualOpt
                .orElseThrow(() -> new RecursoNotFoundException("BolsilloMensual", bolsilloOrigenId));

        BigDecimal comprometidoActual = actual.getMontoComprometido() != null
                ? actual.getMontoComprometido() : BigDecimal.ZERO;
        BigDecimal proyectadoMonto = comprometidoActual.add(nuevoMonto);
        EstadoBolsilloMensual proyectado = EstadoBolsilloMensual.calcular(actual.getBolsillo(), proyectadoMonto);

        BigDecimal limite = actual.getBolsillo().getMontoLimite() != null
                ? actual.getBolsillo().getMontoLimite() : BigDecimal.ZERO;
        BigDecimal restanteProyectado = limite.subtract(proyectadoMonto);

        return PreviewGastoBolsillo.builder()
                .bolsilloOrigenId(bolsilloOrigenId)
                .nombre(actual.getBolsillo().getNombre())
                .color(actual.getBolsillo().getColor())
                .montoLimite(limite)
                .montoNuevoGasto(nuevoMonto)
                .montoGastadoActual(comprometidoActual)
                .porcentajeUsoActual(actual.getPorcentajeUso())
                .montoProyectado(proyectadoMonto)
                .porcentajeUsoProyectado(proyectado.getPorcentajeUso())
                .montoRestanteProyectado(restanteProyectado)
                .nivelActual(actual.getNivel())
                .nivelProyectado(proyectado.getNivel())
                .excedeProyectado(restanteProyectado.signum() < 0)
                .build();
    }

    private Optional<EstadoBolsilloMensual> buscarPorOrigen(EstadoPresupuestoMensual estado,
                                                            UUID bolsilloOrigenId) {
        return estado.getBolsillos().stream()
                .filter(b -> b.getBolsillo().getBolsilloOrigenId() != null
                        && b.getBolsillo().getBolsilloOrigenId().equals(bolsilloOrigenId))
                .findFirst();
    }

    /**
     * Calcula la base del presupuesto del mes:
     * - INGRESOS_MES: suma ingresos del periodo [primer día, último día del mes].
     * - MONTO_FIJO: usa el monto configurado.
     * - INGRESOS_RECURRENTES: suma el monto de las recurrencias de ingreso activas
     *   (sueldo, ingresos fijos esperados), independientemente de si ya se confirmaron.
     */
    private BigDecimal calcularBase(PresupuestoPlantilla plantilla, UUID usuarioId, int anio, int mes) {
        if (plantilla.getTipoBase() == TipoBasePresupuesto.MONTO_FIJO) {
            return plantilla.getMontoFijo();
        }
        if (plantilla.getTipoBase() == TipoBasePresupuesto.INGRESOS_RECURRENTES) {
            return calcularBaseIngresosRecurrentes(usuarioId);
        }
        YearMonth ym = YearMonth.of(anio, mes);
        BigDecimal total = ingresoRepository.sumMontoByUsuarioIdAndFechaBetween(
                usuarioId, ym.atDay(1), ym.atEndOfMonth());
        return total != null ? total : BigDecimal.ZERO;
    }

    private BigDecimal calcularBaseIngresosRecurrentes(UUID usuarioId) {
        return recurrenciaRepository.findActivasByUsuarioId(usuarioId).stream()
                .filter(r -> r.getTipo() == TipoRecurrencia.INGRESO && r.getMonto() != null)
                .map(Recurrencia::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Combina el snapshot mensual con los gastos reales del mes (por mes_facturacion)
     * y las recurrencias de gasto pendientes de confirmar (proyección).
     * Asignación de gastos: 1) bolsilloId explícito; 2) mapeo por categoría.
     * Una recurrencia confirmada en el mes no se proyecta: su gasto generado ya cuenta
     * como gasto real, lo que evita sumar doble el mismo movimiento.
     */
    public EstadoPresupuestoMensual calcularEstado(UUID usuarioId, PresupuestoMensual mensual) {
        YearMonth ym = YearMonth.of(mensual.getAnio(), mensual.getMes());
        LocalDate primerDia = ym.atDay(1);
        List<Gasto> gastos = gastoRepository.findByUsuarioIdAndMesFacturacion(usuarioId, primerDia);

        // Construir mapping categoria -> bolsilloOrigenId desde la plantilla
        Map<CategoriaGasto, UUID> mappingCategoria = new HashMap<>();
        presupuestoRepository.findPlantillaByUsuarioId(usuarioId).ifPresent(plantilla -> {
            for (Bolsillo b : plantilla.getBolsillos()) {
                if (b.getCategorias() != null) {
                    for (CategoriaGasto cat : b.getCategorias()) {
                        mappingCategoria.putIfAbsent(cat, b.getId());
                    }
                }
            }
        });

        // origenId -> bolsilloMensual
        Map<UUID, BolsilloMensual> porOrigen = new HashMap<>();
        for (BolsilloMensual bm : mensual.getBolsillos()) {
            if (bm.getBolsilloOrigenId() != null) {
                porOrigen.put(bm.getBolsilloOrigenId(), bm);
            }
        }

        Map<UUID, BigDecimal> gastoPorBolsillo = new HashMap<>();
        Map<UUID, List<MovimientoBolsillo>> movimientosPorBolsillo = new HashMap<>();
        for (Gasto g : gastos) {
            UUID origenId = g.getBolsilloId();
            if (origenId == null && g.getCategoria() != null) {
                origenId = mappingCategoria.get(g.getCategoria());
            }
            if (origenId == null) continue;
            BolsilloMensual bm = porOrigen.get(origenId);
            if (bm == null) continue;
            gastoPorBolsillo.merge(bm.getId(), g.getMonto(), BigDecimal::add);
            movimientosPorBolsillo.computeIfAbsent(bm.getId(), k -> new ArrayList<>())
                    .add(MovimientoBolsillo.builder()
                            .tipo(MovimientoBolsillo.TipoMovimiento.GASTO)
                            .id(g.getId())
                            .descripcion(g.getDescripcion())
                            .monto(g.getMonto())
                            .fecha(g.getFecha())
                            .categoriaNombre(g.getCategoriaNombre())
                            .build());
        }

        // Recurrencias de gasto activas asignadas a un bolsillo: se proyectan solo si aún
        // no se confirmaron en el mes consultado y el mes no es pasado (la historia se
        // explica solo con gastos reales).
        Map<UUID, BigDecimal> recurrentePorBolsillo = new HashMap<>();
        if (!ym.isBefore(YearMonth.now())) {
            for (Recurrencia r : recurrenciaRepository.findActivasByUsuarioId(usuarioId)) {
                if (r.getTipo() != TipoRecurrencia.GASTO || r.getBolsilloId() == null || r.getMonto() == null) {
                    continue;
                }
                boolean confirmadaEsteMes = r.getUltimaConfirmacionFecha() != null
                        && YearMonth.from(r.getUltimaConfirmacionFecha()).equals(ym);
                if (confirmadaEsteMes) continue;

                BolsilloMensual bm = porOrigen.get(r.getBolsilloId());
                if (bm == null) continue;
                recurrentePorBolsillo.merge(bm.getId(), r.getMonto(), BigDecimal::add);
                movimientosPorBolsillo.computeIfAbsent(bm.getId(), k -> new ArrayList<>())
                        .add(MovimientoBolsillo.builder()
                                .tipo(MovimientoBolsillo.TipoMovimiento.RECURRENCIA)
                                .id(r.getId())
                                .descripcion(r.getDescripcion())
                                .monto(r.getMonto())
                                .fecha(r.getProximaFecha())
                                .categoriaNombre(r.getCategoriaNombre())
                                .build());
            }
        }

        List<EstadoBolsilloMensual> estados = new ArrayList<>();
        BigDecimal totalAsignado = BigDecimal.ZERO;
        BigDecimal totalGastado = BigDecimal.ZERO;
        BigDecimal totalRecurrente = BigDecimal.ZERO;

        for (BolsilloMensual bm : mensual.getBolsillos()) {
            BigDecimal gastado = gastoPorBolsillo.getOrDefault(bm.getId(), BigDecimal.ZERO);
            BigDecimal recurrente = recurrentePorBolsillo.getOrDefault(bm.getId(), BigDecimal.ZERO);
            List<MovimientoBolsillo> movimientos = movimientosPorBolsillo
                    .getOrDefault(bm.getId(), List.of());
            estados.add(EstadoBolsilloMensual.calcular(bm, gastado, recurrente, movimientos));
            totalAsignado = totalAsignado.add(bm.getMontoLimite());
            totalGastado = totalGastado.add(gastado);
            totalRecurrente = totalRecurrente.add(recurrente);
        }

        return EstadoPresupuestoMensual.builder()
                .presupuestoMensualId(mensual.getId())
                .anio(mensual.getAnio())
                .mes(mensual.getMes())
                .baseCalculada(mensual.getBaseCalculada())
                .totalAsignado(totalAsignado)
                .totalGastado(totalGastado)
                .totalRecurrente(totalRecurrente)
                .totalComprometido(totalGastado.add(totalRecurrente))
                .bolsillos(estados)
                .build();
    }

    private void validarPlantilla(PresupuestoPlantilla plantilla) {
        if (plantilla.getTipoBase() == null) {
            throw new DomainException("Debes indicar el tipo de base (INGRESOS_RECURRENTES, INGRESOS_MES o MONTO_FIJO).");
        }
        if (plantilla.getTipoBase() == TipoBasePresupuesto.MONTO_FIJO) {
            if (plantilla.getMontoFijo() == null
                    || plantilla.getMontoFijo().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DomainException("El monto fijo debe ser mayor a cero.");
            }
        } else {
            plantilla.setMontoFijo(null);
        }
        if (plantilla.getBolsillos() == null || plantilla.getBolsillos().isEmpty()) {
            throw new DomainException("Debes definir al menos un bolsillo.");
        }
        BigDecimal suma = plantilla.sumaPorcentajes();
        if (suma.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new DomainException(
                    "La suma de porcentajes (" + suma + "%) supera el 100%.");
        }
    }
}
