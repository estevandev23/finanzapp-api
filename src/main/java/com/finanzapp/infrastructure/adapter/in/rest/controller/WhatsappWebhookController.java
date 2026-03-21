package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.SesionWhatsappService;
import com.finanzapp.domain.exception.SesionWhatsappNoActivaException;
import com.finanzapp.domain.model.*;
import com.finanzapp.domain.port.in.*;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.domain.util.TelefonoUtils;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.ColaMensajeWhatsappEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.ColaMensajeWhatsappJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WhatsApp/N8N", description = "Endpoints para integración con WhatsApp via N8N y MCP")
public class WhatsappWebhookController {

    private final IngresoUseCase ingresoUseCase;
    private final GastoUseCase gastoUseCase;
    private final AhorroUseCase ahorroUseCase;
    private final BalanceUseCase balanceUseCase;
    private final MetaFinancieraUseCase metaUseCase;
    private final DeudaUseCase deudaUseCase;
    private final InversionUseCase inversionUseCase;
    private final DispositivoRepositoryPort dispositivoRepository;
    private final SesionWhatsappService sesionWhatsappService;
    private final ColaMensajeWhatsappJpaRepository colaMensajeRepository;

    @PostMapping("/ingreso")
    @Operation(summary = "Registrar ingreso desde WhatsApp", description = "Endpoint para registrar ingresos desde N8N")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarIngreso(
            @RequestParam String numeroWhatsapp,
            @RequestParam String monto,
            @RequestParam String categoria,
            @RequestParam(required = false) String descripcion) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        BigDecimal montoDecimal = parsearMonto(monto);

        Ingreso ingreso = Ingreso.builder()
                .usuarioId(usuarioId)
                .monto(montoDecimal)
                .categoria(CategoriaIngreso.valueOf(categoria.toUpperCase()))
                .descripcion(descripcion)
                .fecha(LocalDate.now())
                .build();

        Ingreso registrado = ingresoUseCase.registrar(ingreso);

        Map<String, Object> response = Map.of(
                "id", registrado.getId(),
                "monto", registrado.getMonto(),
                "categoria", registrado.getCategoria().getDescripcion(),
                "mensaje", String.format("Ingreso de $%s registrado en %s", montoDecimal, registrado.getCategoria().getDescripcion())
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/gasto")
    @Operation(summary = "Registrar gasto desde WhatsApp", description = "Endpoint para registrar gastos desde N8N")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarGasto(
            @RequestParam String numeroWhatsapp,
            @RequestParam String monto,
            @RequestParam String categoria,
            @RequestParam(required = false) String descripcion) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        BigDecimal montoDecimal = parsearMonto(monto);

        Gasto gasto = Gasto.builder()
                .usuarioId(usuarioId)
                .monto(montoDecimal)
                .categoria(CategoriaGasto.valueOf(categoria.toUpperCase()))
                .descripcion(descripcion)
                .fecha(LocalDate.now())
                .build();

        Gasto registrado = gastoUseCase.registrar(gasto);

        Map<String, Object> response = Map.of(
                "id", registrado.getId(),
                "monto", registrado.getMonto(),
                "categoria", registrado.getCategoria().getDescripcion(),
                "mensaje", String.format("Gasto de $%s registrado en %s", montoDecimal, registrado.getCategoria().getDescripcion())
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ahorro")
    @Operation(summary = "Registrar ahorro desde WhatsApp", description = "Endpoint para registrar ahorros desde N8N")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarAhorro(
            @RequestParam String numeroWhatsapp,
            @RequestParam String monto,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) UUID metaId) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        BigDecimal montoDecimal = parsearMonto(monto);

        Ahorro ahorro = Ahorro.builder()
                .usuarioId(usuarioId)
                .monto(montoDecimal)
                .descripcion(descripcion)
                .metaId(metaId)
                .fecha(LocalDate.now())
                .build();

        Ahorro registrado = ahorroUseCase.registrar(ahorro);

        Map<String, Object> response = Map.of(
                "id", registrado.getId(),
                "monto", registrado.getMonto(),
                "mensaje", String.format("Ahorro de $%s registrado exitosamente", monto)
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/balance")
    @Operation(summary = "Consultar balance desde WhatsApp", description = "Endpoint para consultar balance desde N8N")
    public ResponseEntity<ApiResponse<Map<String, Object>>> consultarBalance(
            @RequestParam String numeroWhatsapp) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        Balance balance = balanceUseCase.obtenerBalanceGeneral(usuarioId);

        Map<String, Object> response = Map.of(
                "totalIngresos", balance.getTotalIngresos(),
                "totalGastos", balance.getTotalGastos(),
                "totalAhorros", balance.getTotalAhorros(),
                "dineroDisponible", balance.getDineroDisponible(),
                "mensaje", String.format(
                        "Tu balance actual:\n" +
                        "- Ingresos: $%s\n" +
                        "- Gastos: $%s\n" +
                        "- Ahorros: $%s\n" +
                        "- Disponible: $%s",
                        balance.getTotalIngresos(),
                        balance.getTotalGastos(),
                        balance.getTotalAhorros(),
                        balance.getDineroDisponible()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/resumen-mes")
    @Operation(summary = "Consultar resumen del mes", description = "Endpoint para consultar resumen mensual desde N8N")
    public ResponseEntity<ApiResponse<Map<String, Object>>> consultarResumenMes(
            @RequestParam String numeroWhatsapp) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();

        Balance balance = balanceUseCase.obtenerBalancePorPeriodo(usuarioId, inicioMes, finMes);
        Map<CategoriaGasto, BigDecimal> desglose = gastoUseCase.obtenerDesglosePorCategoriaPorPeriodo(usuarioId, inicioMes, finMes);

        StringBuilder desgloseStr = new StringBuilder();
        desglose.forEach((cat, total) ->
                desgloseStr.append(String.format("- %s: $%s\n", cat.getDescripcion(), total)));

        Map<String, Object> response = Map.of(
                "periodo", String.format("%s - %s", inicioMes, finMes),
                "totalIngresos", balance.getTotalIngresos(),
                "totalGastos", balance.getTotalGastos(),
                "totalAhorros", balance.getTotalAhorros(),
                "desglosePorCategoria", desglose,
                "mensaje", String.format(
                        "Resumen del mes:\n" +
                        "- Ingresos: $%s\n" +
                        "- Gastos: $%s\n" +
                        "- Ahorros: $%s\n\n" +
                        "Desglose de gastos:\n%s",
                        balance.getTotalIngresos(),
                        balance.getTotalGastos(),
                        balance.getTotalAhorros(),
                        desgloseStr
                )
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/metas")
    @Operation(summary = "Consultar metas activas", description = "Endpoint para consultar metas financieras desde N8N")
    public ResponseEntity<ApiResponse<Map<String, Object>>> consultarMetas(
            @RequestParam String numeroWhatsapp) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        var metas = metaUseCase.listarPorEstado(usuarioId, EstadoMeta.ACTIVA);

        StringBuilder metasStr = new StringBuilder();
        for (MetaFinanciera meta : metas) {
            metasStr.append(String.format(
                    "- %s: $%s de $%s (%.1f%%)\n",
                    meta.getNombre(),
                    meta.getMontoActual(),
                    meta.getMontoObjetivo(),
                    meta.getPorcentajeAvance()
            ));
        }

        Map<String, Object> response = Map.of(
                "cantidadMetas", metas.size(),
                "metas", metas.stream().map(m -> Map.of(
                        "nombre", m.getNombre(),
                        "montoObjetivo", m.getMontoObjetivo(),
                        "montoActual", m.getMontoActual(),
                        "porcentaje", m.getPorcentajeAvance()
                )).toList(),
                "mensaje", metas.isEmpty()
                        ? "No tienes metas activas. ¡Crea una para empezar a ahorrar!"
                        : String.format("Tus metas activas (%d):\n%s", metas.size(), metasStr)
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== DEUDAS Y PRESTAMOS ====================

    @PostMapping("/deuda")
    @Operation(summary = "Crear deuda o prestamo desde WhatsApp",
            description = "Registra una deuda (dinero que debo) o prestamo (dinero que me deben)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> crearDeuda(
            @RequestParam String numeroWhatsapp,
            @RequestParam String tipo,
            @RequestParam String descripcion,
            @RequestParam String montoTotal,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String fechaLimite) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        BigDecimal montoTotalDecimal = parsearMonto(montoTotal);

        TipoDeuda tipoDeuda = TipoDeuda.valueOf(tipo.toUpperCase());

        Deuda deuda = Deuda.builder()
                .usuarioId(usuarioId)
                .tipo(tipoDeuda)
                .descripcion(descripcion)
                .montoTotal(montoTotalDecimal)
                .entidad(entidad)
                .categoria(categoria)
                .fechaInicio(LocalDate.now())
                .fechaLimite(fechaLimite != null ? LocalDate.parse(fechaLimite) : null)
                .build();

        Deuda registrada = deudaUseCase.registrar(deuda);

        String tipoTexto = tipoDeuda == TipoDeuda.DEUDA ? "Deuda" : "Prestamo";
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", registrada.getId());
        response.put("tipo", registrada.getTipo().name());
        response.put("descripcion", registrada.getDescripcion());
        response.put("montoTotal", registrada.getMontoTotal());
        response.put("entidad", registrada.getEntidad());
        response.put("mensaje", String.format("%s de $%s registrada: %s", tipoTexto, montoTotalDecimal, descripcion));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/deudas")
    @Operation(summary = "Consultar deudas y prestamos desde WhatsApp",
            description = "Lista las deudas o prestamos del usuario. Filtrable por tipo (DEUDA/PRESTAMO)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> consultarDeudas(
            @RequestParam String numeroWhatsapp,
            @RequestParam(required = false) String tipo) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);

        List<Deuda> deudas;
        if (tipo != null && !tipo.isBlank()) {
            deudas = deudaUseCase.listarPorTipo(usuarioId, TipoDeuda.valueOf(tipo.toUpperCase()));
        } else {
            deudas = deudaUseCase.listarPorUsuario(usuarioId);
        }

        List<Map<String, Object>> deudasList = deudas.stream().map(d -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", d.getId());
            item.put("tipo", d.getTipo().name());
            item.put("descripcion", d.getDescripcion());
            item.put("entidad", d.getEntidad());
            item.put("montoTotal", d.getMontoTotal());
            item.put("montoAbonado", d.getMontoAbonado());
            item.put("montoRestante", d.getMontoRestante());
            item.put("porcentajeAvance", d.getPorcentajeAvance());
            item.put("estado", d.getEstado().name());
            item.put("fechaLimite", d.getFechaLimite());
            return item;
        }).toList();

        StringBuilder resumen = new StringBuilder();
        for (Deuda d : deudas) {
            String tipoTexto = d.getTipo() == TipoDeuda.DEUDA ? "Deuda" : "Prestamo";
            resumen.append(String.format("- %s: %s | $%s de $%s (%.1f%%)\n",
                    tipoTexto, d.getDescripcion(), d.getMontoAbonado(), d.getMontoTotal(), d.getPorcentajeAvance()));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("cantidad", deudas.size());
        response.put("deudas", deudasList);
        response.put("mensaje", deudas.isEmpty()
                ? "No tienes deudas ni prestamos registrados."
                : String.format("Tienes %d deuda(s)/prestamo(s):\n%s", deudas.size(), resumen));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/deuda/abono")
    @Operation(summary = "Registrar abono a deuda desde WhatsApp",
            description = "Registra un pago parcial o total hacia una deuda o prestamo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> abonarDeuda(
            @RequestParam String deudaId,
            @RequestParam String numeroWhatsapp,
            @RequestParam String monto,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false, defaultValue = "EFECTIVO") String metodoPago) {

        obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        BigDecimal montoDecimal = parsearMonto(monto);
        UUID deudaUUID = UUID.fromString(deudaId.trim());

        MetodoPago metodo = MetodoPago.valueOf(metodoPago.toUpperCase());
        AbonoDeuda abono = deudaUseCase.registrarAbono(deudaUUID, montoDecimal, descripcion, metodo);

        Deuda deudaActualizada = deudaUseCase.obtenerPorId(deudaUUID);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("abonoId", abono.getId());
        response.put("deudaId", deudaUUID);
        response.put("montoAbonado", montoDecimal);
        response.put("montoRestante", deudaActualizada.getMontoRestante());
        response.put("porcentajeAvance", deudaActualizada.getPorcentajeAvance());
        response.put("estado", deudaActualizada.getEstado().name());
        response.put("mensaje", String.format("Abono de $%s registrado. Restante: $%s (%.1f%%)",
                montoDecimal, deudaActualizada.getMontoRestante(), deudaActualizada.getPorcentajeAvance()));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/deudas/resumen")
    @Operation(summary = "Resumen de deudas y prestamos desde WhatsApp",
            description = "Obtiene totales de deudas pendientes, prestamos y abonos recibidos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resumenDeudas(
            @RequestParam String numeroWhatsapp) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);

        BigDecimal totalDeudas = deudaUseCase.obtenerTotalDeudas(usuarioId);
        BigDecimal totalPrestamos = deudaUseCase.obtenerTotalPrestamos(usuarioId);
        BigDecimal abonosRecibidos = deudaUseCase.obtenerTotalAbonosPrestamosRecibidos(usuarioId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalDeudas", totalDeudas);
        response.put("totalPrestamos", totalPrestamos);
        response.put("abonosRecibidos", abonosRecibidos);
        response.put("mensaje", String.format(
                "Resumen de deudas:\n" +
                "- Deudas pendientes: $%s\n" +
                "- Prestamos por cobrar: $%s\n" +
                "- Abonos recibidos: $%s",
                totalDeudas, totalPrestamos, abonosRecibidos));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/deuda/abonos")
    @Operation(summary = "Historial de abonos de una deuda desde WhatsApp",
            description = "Lista todos los abonos realizados a una deuda o prestamo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> historialAbonos(
            @RequestParam String deudaId,
            @RequestParam String numeroWhatsapp) {

        obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        UUID deudaUUID = UUID.fromString(deudaId.trim());

        List<AbonoDeuda> abonos = deudaUseCase.listarAbonos(deudaUUID);
        Deuda deuda = deudaUseCase.obtenerPorId(deudaUUID);

        List<Map<String, Object>> abonosList = abonos.stream().map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("monto", a.getMonto());
            item.put("descripcion", a.getDescripcion());
            item.put("fecha", a.getFechaAbono());
            return item;
        }).toList();

        StringBuilder resumen = new StringBuilder();
        for (AbonoDeuda a : abonos) {
            resumen.append(String.format("- $%s el %s\n", a.getMonto(), a.getFechaAbono().toLocalDate()));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deudaDescripcion", deuda.getDescripcion());
        response.put("cantidadAbonos", abonos.size());
        response.put("abonos", abonosList);
        response.put("mensaje", abonos.isEmpty()
                ? String.format("No hay abonos registrados para: %s", deuda.getDescripcion())
                : String.format("Abonos de '%s' (%d):\n%s", deuda.getDescripcion(), abonos.size(), resumen));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== INVERSIONES ====================

    @PostMapping("/inversion")
    @Operation(summary = "Crear inversion desde WhatsApp",
            description = "Registra una nueva inversion con monto y retorno esperado opcional")
    public ResponseEntity<ApiResponse<Map<String, Object>>> crearInversion(
            @RequestParam String numeroWhatsapp,
            @RequestParam String nombre,
            @RequestParam String monto,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String retornoEsperado) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        BigDecimal montoDecimal = parsearMonto(monto);
        BigDecimal retornoEsperadoDecimal = retornoEsperado != null && !retornoEsperado.isBlank()
                ? parsearMonto(retornoEsperado) : null;

        Inversion inversion = Inversion.builder()
                .usuarioId(usuarioId)
                .nombre(nombre)
                .monto(montoDecimal)
                .descripcion(descripcion)
                .retornoEsperado(retornoEsperadoDecimal)
                .fechaInversion(LocalDate.now())
                .build();

        Inversion creada = inversionUseCase.crear(inversion);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", creada.getId());
        response.put("nombre", creada.getNombre());
        response.put("monto", creada.getMonto());
        response.put("retornoEsperado", creada.getRetornoEsperado());
        response.put("mensaje", String.format("Inversion '%s' de $%s registrada exitosamente", nombre, montoDecimal));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/inversiones")
    @Operation(summary = "Consultar inversiones desde WhatsApp",
            description = "Lista las inversiones del usuario. Filtrable por estado (ACTIVA/FINALIZADA)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> consultarInversiones(
            @RequestParam String numeroWhatsapp,
            @RequestParam(required = false) String estado) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);

        List<Inversion> inversiones;
        if (estado != null && !estado.isBlank()) {
            inversiones = inversionUseCase.listarPorEstado(usuarioId, EstadoInversion.valueOf(estado.toUpperCase()));
        } else {
            inversiones = inversionUseCase.listarPorUsuario(usuarioId);
        }

        List<Map<String, Object>> inversionesList = inversiones.stream().map(inv -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", inv.getId());
            item.put("nombre", inv.getNombre());
            item.put("monto", inv.getMonto());
            item.put("retornoEsperado", inv.getRetornoEsperado());
            item.put("retornoReal", inv.getRetornoReal());
            item.put("ganancia", inv.calcularGanancia());
            item.put("estado", inv.getEstado().name());
            item.put("fechaInversion", inv.getFechaInversion());
            return item;
        }).toList();

        StringBuilder resumen = new StringBuilder();
        for (Inversion inv : inversiones) {
            String estadoTexto = inv.getEstado() == EstadoInversion.ACTIVA ? "Activa" : "Finalizada";
            resumen.append(String.format("- %s: $%s (%s)\n", inv.getNombre(), inv.getMonto(), estadoTexto));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("cantidad", inversiones.size());
        response.put("inversiones", inversionesList);
        response.put("mensaje", inversiones.isEmpty()
                ? "No tienes inversiones registradas."
                : String.format("Tus inversiones (%d):\n%s", inversiones.size(), resumen));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/inversion/retorno")
    @Operation(summary = "Registrar retorno de inversion desde WhatsApp",
            description = "Registra el retorno real de una inversion y la marca como finalizada")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarRetornoInversion(
            @RequestParam String inversionId,
            @RequestParam String numeroWhatsapp,
            @RequestParam String retornoReal) {

        obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);
        BigDecimal retornoRealDecimal = parsearMonto(retornoReal);
        UUID inversionUUID = UUID.fromString(inversionId.trim());

        Inversion actualizada = inversionUseCase.registrarRetorno(inversionUUID, retornoRealDecimal, LocalDate.now());

        BigDecimal ganancia = actualizada.calcularGanancia();
        String resultadoTexto = ganancia.compareTo(BigDecimal.ZERO) >= 0 ? "ganancia" : "perdida";

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", actualizada.getId());
        response.put("nombre", actualizada.getNombre());
        response.put("montoInvertido", actualizada.getMonto());
        response.put("retornoReal", retornoRealDecimal);
        response.put("ganancia", ganancia);
        response.put("estado", actualizada.getEstado().name());
        response.put("mensaje", String.format("Retorno de $%s registrado para '%s'. %s de $%s",
                retornoRealDecimal, actualizada.getNombre(),
                resultadoTexto.substring(0, 1).toUpperCase() + resultadoTexto.substring(1),
                ganancia.abs()));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== ANALISIS FINANCIERO ====================

    @GetMapping("/analisis")
    @Operation(summary = "Analisis financiero completo desde WhatsApp",
            description = "Obtiene un analisis financiero integral: balance, desglose, metas, deudas e inversiones")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analisisFinanciero(
            @RequestParam String numeroWhatsapp,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);

        Balance balance = balanceUseCase.obtenerBalanceGeneral(usuarioId);
        Map<CategoriaGasto, BigDecimal> desglose = gastoUseCase.obtenerDesglosePorCategoria(usuarioId);
        var metasActivas = metaUseCase.listarPorEstado(usuarioId, EstadoMeta.ACTIVA);
        BigDecimal totalDeudas = deudaUseCase.obtenerTotalDeudas(usuarioId);
        BigDecimal totalPrestamos = deudaUseCase.obtenerTotalPrestamos(usuarioId);
        List<Inversion> inversionesActivas = inversionUseCase.listarPorEstado(usuarioId, EstadoInversion.ACTIVA);

        BigDecimal totalInvertido = inversionesActivas.stream()
                .map(Inversion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tasaAhorro = BigDecimal.ZERO;
        if (balance.getTotalIngresos().compareTo(BigDecimal.ZERO) > 0) {
            tasaAhorro = balance.getTotalAhorros()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(balance.getTotalIngresos(), 2, java.math.RoundingMode.HALF_UP);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalIngresos", balance.getTotalIngresos());
        response.put("totalGastos", balance.getTotalGastos());
        response.put("totalAhorros", balance.getTotalAhorros());
        response.put("dineroDisponible", balance.getDineroDisponible());
        response.put("totalDeudas", totalDeudas);
        response.put("totalPrestamos", totalPrestamos);
        response.put("totalInvertido", totalInvertido);
        response.put("inversionesActivas", inversionesActivas.size());
        response.put("tasaAhorro", tasaAhorro);
        response.put("metasActivas", metasActivas.size());
        response.put("desglosePorCategoria", desglose);

        StringBuilder analisis = new StringBuilder();
        analisis.append("Analisis financiero:\n\n");
        analisis.append(String.format("Balance: Ingresos $%s | Gastos $%s | Disponible $%s\n",
                balance.getTotalIngresos(), balance.getTotalGastos(), balance.getDineroDisponible()));
        analisis.append(String.format("Ahorros: $%s (tasa: %.1f%%)\n", balance.getTotalAhorros(), tasaAhorro));

        if (totalDeudas.compareTo(BigDecimal.ZERO) > 0 || totalPrestamos.compareTo(BigDecimal.ZERO) > 0) {
            analisis.append(String.format("Deudas: $%s | Prestamos por cobrar: $%s\n", totalDeudas, totalPrestamos));
        }
        if (!inversionesActivas.isEmpty()) {
            analisis.append(String.format("Inversiones activas: %d por $%s\n", inversionesActivas.size(), totalInvertido));
        }
        if (!metasActivas.isEmpty()) {
            analisis.append(String.format("Metas activas: %d\n", metasActivas.size()));
            for (MetaFinanciera m : metasActivas) {
                analisis.append(String.format("  - %s: %.1f%%\n", m.getNombre(), m.getPorcentajeAvance()));
            }
        }
        if (!desglose.isEmpty()) {
            analisis.append("\nDesglose de gastos:\n");
            desglose.forEach((cat, total) ->
                    analisis.append(String.format("  - %s: $%s\n", cat.getDescripcion(), total)));
        }

        response.put("mensaje", analisis.toString());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========================
    // Cola de mensajes WhatsApp (debounce)
    // ========================

    @PostMapping("/cola/guardar")
    @Operation(summary = "Guardar mensaje en cola", description = "Almacena un mensaje de WhatsApp en la cola para debounce. No requiere sesion activa.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> guardarMensajeEnCola(
            @RequestParam String telefono,
            @RequestParam String texto,
            @RequestParam(required = false) String contacto,
            @RequestParam(required = false) String messageId,
            @RequestParam(required = false, defaultValue = "false") Boolean esAudio) {

        ColaMensajeWhatsappEntity mensaje = ColaMensajeWhatsappEntity.builder()
                .numeroTelefono(telefono)
                .textoMensaje(texto)
                .nombreContacto(contacto)
                .messageId(messageId)
                .esAudio(esAudio)
                .recibidoEn(LocalDateTime.now())
                .procesado(false)
                .build();

        mensaje = colaMensajeRepository.save(mensaje);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", mensaje.getId());
        data.put("recibidoEn", mensaje.getRecibidoEn().toString());

        return ResponseEntity.ok(ApiResponse.success(data, "Mensaje guardado en cola"));
    }

    @PostMapping("/cola/procesar")
    @Transactional
    @Operation(summary = "Procesar cola de mensajes", description = "Verifica si hay mensajes mas recientes. Si no, combina todos los pendientes y los marca como procesados.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> procesarColaMensajes(
            @RequestParam String telefono,
            @RequestParam Long colaId) {

        long masRecientes = colaMensajeRepository
                .countByNumeroTelefonoAndProcesadoFalseAndIdGreaterThan(telefono, colaId);

        Map<String, Object> data = new LinkedHashMap<>();

        if (masRecientes > 0) {
            data.put("hayMasRecientes", true);
            data.put("textoCombinado", null);
            data.put("cantidadMensajes", 0);
            return ResponseEntity.ok(ApiResponse.success(data, "Hay mensajes mas recientes pendientes"));
        }

        List<ColaMensajeWhatsappEntity> pendientes = colaMensajeRepository
                .findByNumeroTelefonoAndProcesadoFalseOrderByRecibidoEnAsc(telefono);

        if (pendientes.isEmpty()) {
            data.put("hayMasRecientes", false);
            data.put("textoCombinado", null);
            data.put("cantidadMensajes", 0);
            return ResponseEntity.ok(ApiResponse.success(data, "No hay mensajes pendientes"));
        }

        String textoCombinado;
        if (pendientes.size() == 1) {
            textoCombinado = pendientes.get(0).getTextoMensaje();
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pendientes.size(); i++) {
                sb.append("Mensaje ").append(i + 1).append(": ")
                  .append(pendientes.get(i).getTextoMensaje());
                if (i < pendientes.size() - 1) {
                    sb.append("\n");
                }
            }
            textoCombinado = sb.toString();
        }

        colaMensajeRepository.marcarComoProcesados(telefono);

        data.put("hayMasRecientes", false);
        data.put("textoCombinado", textoCombinado);
        data.put("cantidadMensajes", pendientes.size());

        return ResponseEntity.ok(ApiResponse.success(data, "Mensajes procesados"));
    }

    /**
     * Valida que exista una sesion activa para el numero de WhatsApp,
     * actualiza la ultima actividad y retorna el usuarioId asociado.
     */
    private UUID obtenerUsuarioIdPorWhatsapp(String numeroWhatsapp) {
        String telefono = TelefonoUtils.normalizar(numeroWhatsapp);
        SesionWhatsapp sesion = sesionWhatsappService.verificarSesion(telefono)
                .orElseThrow(SesionWhatsappNoActivaException::new);

        sesionWhatsappService.actualizarActividad(telefono);
        return sesion.getUsuarioId();
    }

    /**
     * Parsea un monto desde texto libre (puede venir con formato del AI: "$50,000", "50.000", "50000").
     * Limpia caracteres no numericos y convierte a BigDecimal.
     */
    private BigDecimal parsearMonto(String montoTexto) {
        if (montoTexto == null || montoTexto.isBlank()) {
            throw new IllegalArgumentException("El monto es obligatorio y no puede estar vacio");
        }

        String limpio = montoTexto.trim()
                .replace("$", "")
                .replace(" ", "")
                .replace("_", "");

        // Si tiene tanto puntos como comas, asumir formato con separador de miles
        if (limpio.contains(",") && limpio.contains(".")) {
            if (limpio.lastIndexOf(",") > limpio.lastIndexOf(".")) {
                // Coma es separador decimal (formato europeo/colombiano): 1.000.000,50
                limpio = limpio.replace(".", "").replace(",", ".");
            } else {
                // Punto es separador decimal (formato US): 1,000,000.50
                limpio = limpio.replace(",", "");
            }
        } else if (limpio.contains(",")) {
            long comaCount = limpio.chars().filter(c -> c == ',').count();
            if (comaCount == 1 && limpio.indexOf(",") >= limpio.length() - 3) {
                limpio = limpio.replace(",", ".");
            } else {
                limpio = limpio.replace(",", "");
            }
        } else if (limpio.contains(".")) {
            // Solo puntos: verificar si es separador de miles (patron: grupos de 3 digitos)
            long dotCount = limpio.chars().filter(c -> c == '.').count();
            String despuesPunto = limpio.substring(limpio.lastIndexOf(".") + 1);
            if (dotCount >= 1 && despuesPunto.length() == 3 && !limpio.startsWith("0.")) {
                // Patron de miles: "50.000", "1.000.000"
                limpio = limpio.replace(".", "");
            }
        }

        try {
            BigDecimal resultado = new BigDecimal(limpio);
            if (resultado.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El monto debe ser mayor a cero");
            }
            return resultado;
        } catch (NumberFormatException e) {
            log.warn("No se pudo parsear el monto recibido: '{}' (limpio: '{}')", montoTexto, limpio);
            throw new IllegalArgumentException(
                    String.format("El monto '%s' no es un valor numerico valido", montoTexto));
        }
    }
}
