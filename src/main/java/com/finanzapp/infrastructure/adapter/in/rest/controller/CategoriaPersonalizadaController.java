package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.domain.model.CategoriaPersonalizada;
import com.finanzapp.domain.model.TipoCategoria;
import com.finanzapp.domain.port.in.CategoriaPersonalizadaUseCase;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.categoria.CategoriaPersonalizadaRequest;
import com.finanzapp.infrastructure.adapter.in.rest.dto.categoria.CategoriaPersonalizadaResponse;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías personalizadas", description = "Gestión de categorías personalizadas del usuario")
public class CategoriaPersonalizadaController {

    private final CategoriaPersonalizadaUseCase categoriaUseCase;

    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría personalizada")
    public ResponseEntity<ApiResponse<CategoriaPersonalizadaResponse>> crear(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CategoriaPersonalizadaRequest request) {

        CategoriaPersonalizada categoria = CategoriaPersonalizada.builder()
                .usuarioId(userDetails.getId())
                .nombre(request.getNombre())
                .tipo(request.getTipo())
                .color(request.getColor())
                .icono(request.getIcono())
                .build();

        CategoriaPersonalizada creada = categoriaUseCase.crear(categoria);
        return ResponseEntity.ok(ApiResponse.success(
                CategoriaPersonalizadaResponse.fromDomain(creada), "Categoría creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Lista todas las categorías personalizadas del usuario")
    public ResponseEntity<ApiResponse<List<CategoriaPersonalizadaResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<CategoriaPersonalizadaResponse> categorias = categoriaUseCase
                .listarPorUsuario(userDetails.getId())
                .stream()
                .map(CategoriaPersonalizadaResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(categorias));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar por tipo", description = "Lista categorías personalizadas filtradas por tipo (INGRESO o GASTO)")
    public ResponseEntity<ApiResponse<List<CategoriaPersonalizadaResponse>>> listarPorTipo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable TipoCategoria tipo) {

        List<CategoriaPersonalizadaResponse> categorias = categoriaUseCase
                .listarPorUsuarioYTipo(userDetails.getId(), tipo)
                .stream()
                .map(CategoriaPersonalizadaResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(categorias));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría personalizada existente")
    public ResponseEntity<ApiResponse<CategoriaPersonalizadaResponse>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CategoriaPersonalizadaRequest request) {

        CategoriaPersonalizada categoria = CategoriaPersonalizada.builder()
                .nombre(request.getNombre())
                .color(request.getColor())
                .icono(request.getIcono())
                .build();

        CategoriaPersonalizada actualizada = categoriaUseCase.actualizar(id, categoria);
        return ResponseEntity.ok(ApiResponse.success(
                CategoriaPersonalizadaResponse.fromDomain(actualizada), "Categoría actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría personalizada")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        categoriaUseCase.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Categoría eliminada exitosamente"));
    }
}
