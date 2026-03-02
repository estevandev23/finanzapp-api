package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.finanzapp.infrastructure.adapter.in.rest.dto.pais.PrefijoPaisResponse;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.PrefijoPaisJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/paises")
@RequiredArgsConstructor
@Tag(name = "Países", description = "Datos de referencia para prefijos telefónicos por país")
public class PrefijoPaisController {

    private final PrefijoPaisJpaRepository prefijoPaisRepository;

    @GetMapping("/prefijos")
    @Operation(summary = "Listar prefijos activos", description = "Retorna todos los prefijos telefónicos de países activos en el sistema")
    public ResponseEntity<ApiResponse<List<PrefijoPaisResponse>>> listarPrefijos() {
        List<PrefijoPaisResponse> prefijos = prefijoPaisRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(e -> new PrefijoPaisResponse(
                        e.getId(),
                        e.getNombre(),
                        e.getCodigoIso(),
                        e.getPrefijoTelefono(),
                        e.getBanderaEmoji()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(prefijos, "Prefijos obtenidos exitosamente"));
    }
}
