package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.RecomendacionService;
import com.finanzapp.application.service.RecomendacionService.RecomendacionResult;
import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.recomendacion.RecomendacionResponse;
import com.finanzapp.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recomendaciones")
@RequiredArgsConstructor
@Tag(name = "Recomendaciones", description = "Recomendaciones financieras generadas por IA")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    @GetMapping
    @Operation(summary = "Obtener recomendación", description = "Obtiene la recomendación financiera actual. Se auto-genera una vez por hora.")
    public ResponseEntity<ApiResponse<RecomendacionResponse>> obtener(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        RecomendacionResult result = recomendacionService.obtenerRecomendacion(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(RecomendacionResponse.from(result)));
    }

    @PostMapping("/regenerar")
    @Operation(summary = "Regenerar recomendación", description = "Genera una nueva recomendación manualmente. Máximo 3 regeneraciones por hora.")
    public ResponseEntity<ApiResponse<RecomendacionResponse>> regenerar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        RecomendacionResult result = recomendacionService.regenerarRecomendacion(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(RecomendacionResponse.from(result), "Recomendación generada exitosamente"));
    }
}
