package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.SesionWhatsappService;
import com.finanzapp.domain.exception.SesionWhatsappNoActivaException;
import com.finanzapp.domain.model.*;
import com.finanzapp.domain.port.in.*;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.domain.util.TelefonoUtils;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

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
    private final DispositivoRepositoryPort dispositivoRepository;
    private final SesionWhatsappService sesionWhatsappService;

    @PostMapping("/ingreso")
    @Operation(summary = "Registrar ingreso desde WhatsApp", description = "Endpoint para registrar ingresos desde N8N")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarIngreso(
            @RequestParam String numeroWhatsapp,
            @RequestParam BigDecimal monto,
            @RequestParam String categoria,
            @RequestParam(required = false) String descripcion) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);

        Ingreso ingreso = Ingreso.builder()
                .usuarioId(usuarioId)
                .monto(monto)
                .categoria(CategoriaIngreso.valueOf(categoria.toUpperCase()))
                .descripcion(descripcion)
                .fecha(LocalDate.now())
                .build();

        Ingreso registrado = ingresoUseCase.registrar(ingreso);

        Map<String, Object> response = Map.of(
                "id", registrado.getId(),
                "monto", registrado.getMonto(),
                "categoria", registrado.getCategoria().getDescripcion(),
                "mensaje", String.format("Ingreso de $%s registrado en %s", monto, registrado.getCategoria().getDescripcion())
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/gasto")
    @Operation(summary = "Registrar gasto desde WhatsApp", description = "Endpoint para registrar gastos desde N8N")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarGasto(
            @RequestParam String numeroWhatsapp,
            @RequestParam BigDecimal monto,
            @RequestParam String categoria,
            @RequestParam(required = false) String descripcion) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);

        Gasto gasto = Gasto.builder()
                .usuarioId(usuarioId)
                .monto(monto)
                .categoria(CategoriaGasto.valueOf(categoria.toUpperCase()))
                .descripcion(descripcion)
                .fecha(LocalDate.now())
                .build();

        Gasto registrado = gastoUseCase.registrar(gasto);

        Map<String, Object> response = Map.of(
                "id", registrado.getId(),
                "monto", registrado.getMonto(),
                "categoria", registrado.getCategoria().getDescripcion(),
                "mensaje", String.format("Gasto de $%s registrado en %s", monto, registrado.getCategoria().getDescripcion())
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ahorro")
    @Operation(summary = "Registrar ahorro desde WhatsApp", description = "Endpoint para registrar ahorros desde N8N")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrarAhorro(
            @RequestParam String numeroWhatsapp,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) UUID metaId) {

        UUID usuarioId = obtenerUsuarioIdPorWhatsapp(numeroWhatsapp);

        Ahorro ahorro = Ahorro.builder()
                .usuarioId(usuarioId)
                .monto(monto)
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
}
