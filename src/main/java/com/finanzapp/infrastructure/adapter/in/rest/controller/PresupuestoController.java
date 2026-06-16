package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.Bolsillo;
import com.finanzapp.domain.model.BolsilloMensual;
import com.finanzapp.domain.model.EstadoPresupuestoMensual;
import com.finanzapp.domain.model.PresupuestoMensual;
import com.finanzapp.domain.model.PresupuestoPlantilla;
import com.finanzapp.domain.model.PreviewGastoBolsillo;
import com.finanzapp.domain.port.in.PresupuestoUseCase;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto.BolsilloMensualOverrideRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto.BolsilloRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto.BolsilloResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto.EstadoPresupuestoResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto.PlantillaPresupuestoRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto.PlantillaPresupuestoResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.presupuesto.PreviewGastoResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/presupuesto")
@RequiredArgsConstructor
@Tag(name = "Presupuesto", description = "Gestión de plantilla, bolsillos y estado mensual del presupuesto")
public class PresupuestoController {

    private final PresupuestoUseCase presupuestoUseCase;

    @GetMapping("/plantilla")
    @Operation(summary = "Obtener plantilla del usuario")
    public ResponseEntity<ApiResponse<PlantillaPresupuestoResponse>> obtenerPlantilla(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return presupuestoUseCase.obtenerPlantilla(userDetails.getId())
                .map(p -> ResponseEntity.ok(ApiResponse.success(PlantillaPresupuestoResponse.fromDomain(p))))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success(null, "Aún no hay plantilla configurada")));
    }

    @PutMapping("/plantilla")
    @Operation(summary = "Crear o reemplazar plantilla",
            description = "Reemplaza completamente la plantilla del usuario y sus bolsillos")
    public ResponseEntity<ApiResponse<PlantillaPresupuestoResponse>> guardarPlantilla(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PlantillaPresupuestoRequest request) {

        List<Bolsillo> bolsillos = new ArrayList<>();
        if (request.getBolsillos() != null) {
            for (BolsilloRequest br : request.getBolsillos()) {
                bolsillos.add(Bolsillo.builder()
                        .id(br.getId())
                        .nombre(br.getNombre())
                        .porcentaje(br.getPorcentaje())
                        .tipo(br.getTipo())
                        .color(br.getColor())
                        .orden(br.getOrden())
                        .categorias(br.getCategorias())
                        .build());
            }
        }

        PresupuestoPlantilla plantilla = PresupuestoPlantilla.builder()
                .tipoBase(request.getTipoBase())
                .montoFijo(request.getMontoFijo())
                .bolsillos(bolsillos)
                .build();

        PresupuestoPlantilla guardada = presupuestoUseCase.guardarPlantilla(userDetails.getId(), plantilla);
        return ResponseEntity.ok(ApiResponse.success(
                PlantillaPresupuestoResponse.fromDomain(guardada),
                "Plantilla de presupuesto guardada"));
    }

    @GetMapping("/estado")
    @Operation(summary = "Estado mensual del presupuesto",
            description = "Devuelve los bolsillos con gasto acumulado y nivel (OK/ADVERTENCIA/EXCEDIDO)")
    public ResponseEntity<ApiResponse<EstadoPresupuestoResponse>> estadoMensual(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes) {
        LocalDate hoy = LocalDate.now();
        int a = anio != null ? anio : hoy.getYear();
        int m = mes != null ? mes : hoy.getMonthValue();

        EstadoPresupuestoMensual estado = presupuestoUseCase.obtenerEstadoMensual(userDetails.getId(), a, m);
        return ResponseEntity.ok(ApiResponse.success(EstadoPresupuestoResponse.fromDomain(estado)));
    }

    @PostMapping("/mensual/regenerar")
    @Operation(summary = "Regenerar snapshot mensual",
            description = "Fuerza la recreación del snapshot del mes a partir de la plantilla actual")
    public ResponseEntity<ApiResponse<EstadoPresupuestoResponse>> regenerar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Integer anio,
            @RequestParam Integer mes) {
        PresupuestoMensual mensual = presupuestoUseCase.regenerarMensual(userDetails.getId(), anio, mes);
        EstadoPresupuestoMensual estado = presupuestoUseCase.obtenerEstadoMensual(userDetails.getId(),
                mensual.getAnio(), mensual.getMes());
        return ResponseEntity.ok(ApiResponse.success(EstadoPresupuestoResponse.fromDomain(estado),
                "Presupuesto mensual regenerado"));
    }

    @PatchMapping("/bolsillo-mensual/{id}")
    @Operation(summary = "Override de bolsillo dentro del mes",
            description = "Permite ajustar porcentaje o monto límite de un bolsillo solo para ese mes")
    public ResponseEntity<ApiResponse<BolsilloResponse>> overrideBolsilloMensual(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody BolsilloMensualOverrideRequest request) {
        BolsilloMensual actualizado = presupuestoUseCase.actualizarBolsilloMensual(
                userDetails.getId(), id, request.getPorcentaje(), request.getMontoLimite());

        BolsilloResponse response = BolsilloResponse.builder()
                .id(actualizado.getId())
                .nombre(actualizado.getNombre())
                .porcentaje(actualizado.getPorcentaje())
                .tipo(actualizado.getTipo())
                .tipoDescripcion(actualizado.getTipo() != null ? actualizado.getTipo().getDescripcion() : null)
                .color(actualizado.getColor())
                .orden(actualizado.getOrden())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response, "Bolsillo del mes actualizado"));
    }

    @GetMapping("/preview-gasto")
    @Operation(summary = "Previsualizar impacto de un gasto en un bolsillo",
            description = "Devuelve el porcentaje ya consumido del bolsillo y el que quedaría al " +
                    "registrar un gasto del monto indicado. Si no se pasan año/mes se usa el mes actual.")
    public ResponseEntity<ApiResponse<PreviewGastoResponse>> previewGasto(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam UUID bolsilloId,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes) {
        LocalDate hoy = LocalDate.now();
        int a = anio != null ? anio : hoy.getYear();
        int m = mes != null ? mes : hoy.getMonthValue();

        PreviewGastoBolsillo preview = presupuestoUseCase.previsualizarGasto(
                userDetails.getId(), bolsilloId, monto, a, m);
        return ResponseEntity.ok(ApiResponse.success(PreviewGastoResponse.fromDomain(preview)));
    }
}
