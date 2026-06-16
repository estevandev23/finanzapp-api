package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.RecurrenciaService;
import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoRecurrencia;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia.ConfirmarRecurrenciaRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia.ConfirmarRecurrenciaResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia.RecurrenciaRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia.RecurrenciaResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.recurrencia.RecurrenciaUpdateRequest;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurrencias")
@RequiredArgsConstructor
@Tag(name = "Recurrencias", description = "Plantillas reutilizables de ingresos y gastos (sueldo, prima, suscripciones)")
public class RecurrenciaController {

    private final RecurrenciaService recurrenciaService;

    @PostMapping
    @Operation(summary = "Crear recurrencia",
            description = "Crea una plantilla de ingreso o gasto que se repite con la frecuencia indicada. " +
                    "No genera registros reales hasta que el usuario la confirme con el endpoint /confirmar.")
    public ResponseEntity<ApiResponse<RecurrenciaResponse>> crear(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecurrenciaRequest request) {

        Recurrencia recurrencia = Recurrencia.builder()
                .usuarioId(userDetails.getId())
                .tipo(request.getTipo())
                .frecuencia(request.getFrecuencia())
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .categoriaIngreso(request.getCategoriaIngreso())
                .categoriaGasto(request.getCategoriaGasto())
                .categoriaPersonalizadaId(request.getCategoriaPersonalizadaId())
                .metodoPago(request.getMetodoPago())
                .tarjetaId(request.getTarjetaId())
                .bolsilloId(request.getBolsilloId())
                .diaVencimiento(request.getDiaVencimiento())
                .mesReferencia(request.getMesReferencia())
                .proximaFecha(request.getProximaFecha())
                .build();

        Recurrencia creada = recurrenciaService.crear(recurrencia);
        return ResponseEntity.ok(ApiResponse.success(
                RecurrenciaResponse.fromDomain(creada),
                "Recurrencia creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar recurrencias del usuario",
            description = "Lista todas las recurrencias del usuario. Filtros opcionales por tipo (INGRESO/GASTO) " +
                    "y solo activas (activas=true).")
    public ResponseEntity<ApiResponse<List<RecurrenciaResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) TipoRecurrencia tipo,
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas) {

        UUID usuarioId = userDetails.getId();
        List<Recurrencia> recurrencias;
        if (tipo != null) {
            recurrencias = recurrenciaService.listarPorTipo(usuarioId, tipo);
        } else if (soloActivas) {
            recurrencias = recurrenciaService.listarActivasPorUsuario(usuarioId);
        } else {
            recurrencias = recurrenciaService.listarPorUsuario(usuarioId);
        }

        return ResponseEntity.ok(ApiResponse.success(
                recurrencias.stream().map(RecurrenciaResponse::fromDomain).toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener recurrencia por id")
    public ResponseEntity<ApiResponse<RecurrenciaResponse>> obtener(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        Recurrencia recurrencia = recurrenciaService.obtenerPorIdValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(RecurrenciaResponse.fromDomain(recurrencia)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar recurrencia")
    public ResponseEntity<ApiResponse<RecurrenciaResponse>> actualizar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody RecurrenciaUpdateRequest request) {

        Recurrencia datos = Recurrencia.builder()
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .frecuencia(request.getFrecuencia())
                .categoriaIngreso(request.getCategoriaIngreso())
                .categoriaGasto(request.getCategoriaGasto())
                .categoriaPersonalizadaId(request.getCategoriaPersonalizadaId())
                .metodoPago(request.getMetodoPago())
                .tarjetaId(request.getTarjetaId())
                .bolsilloId(request.getBolsilloId())
                .diaVencimiento(request.getDiaVencimiento() != null ? request.getDiaVencimiento() : 0)
                .mesReferencia(request.getMesReferencia())
                .proximaFecha(request.getProximaFecha())
                .build();

        Recurrencia actualizada = recurrenciaService.actualizarValidado(id, datos, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(
                RecurrenciaResponse.fromDomain(actualizada),
                "Recurrencia actualizada exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar recurrencia",
            description = "Pasa la recurrencia entre activa (true) e inactiva (false). " +
                    "Las inactivas no se pueden confirmar y no aparecen en recordatorios.")
    public ResponseEntity<ApiResponse<RecurrenciaResponse>> cambiarEstado(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @RequestParam boolean activa) {
        Recurrencia actualizada = recurrenciaService.cambiarEstadoValidado(id, activa, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(
                RecurrenciaResponse.fromDomain(actualizada),
                activa ? "Recurrencia activada" : "Recurrencia pausada"));
    }

    @PostMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar recurrencia",
            description = "Genera el ingreso o gasto real (con la fecha indicada o la actual) y avanza la próxima fecha. " +
                    "Úsalo cuando el usuario diga 'ya me pagaron' o 'ya pagué la cuota'.")
    public ResponseEntity<ApiResponse<ConfirmarRecurrenciaResponse>> confirmar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody(required = false) ConfirmarRecurrenciaRequest request) {

        java.time.LocalDate fecha = request != null ? request.getFechaConfirmacion() : null;
        UUID registroId = recurrenciaService.confirmarValidado(id, fecha, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(
                ConfirmarRecurrenciaResponse.builder()
                        .recurrenciaId(id)
                        .registroGeneradoId(registroId)
                        .mensaje("Recurrencia confirmada y registro generado")
                        .build(),
                "Recurrencia confirmada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar recurrencia")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        recurrenciaService.eliminarValidado(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Recurrencia eliminada exitosamente"));
    }
}
