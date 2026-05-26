package com.finanzapp.application.service;

import com.finanzapp.domain.model.Balance;
import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.EstadoMeta;
import com.finanzapp.domain.model.MetaFinanciera;
import com.finanzapp.domain.port.in.BalanceUseCase;
import com.finanzapp.domain.port.in.GastoUseCase;
import com.finanzapp.domain.port.out.MetaFinancieraRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RecomendacionService {

    private final BalanceUseCase balanceUseCase;
    private final GastoUseCase gastoUseCase;
    private final MetaFinancieraRepositoryPort metaRepository;
    private final RestTemplate restTemplate;

    @Value("${openai.api-key:}")
    private String openaiApiKey;

    private static final int MAX_MANUAL_PER_HOUR = 3;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final ConcurrentHashMap<UUID, RecomendacionCache> cache = new ConcurrentHashMap<>();

    public RecomendacionService(BalanceUseCase balanceUseCase,
                                 GastoUseCase gastoUseCase,
                                 MetaFinancieraRepositoryPort metaRepository) {
        this.balanceUseCase = balanceUseCase;
        this.gastoUseCase = gastoUseCase;
        this.metaRepository = metaRepository;
        this.restTemplate = new RestTemplate();
    }

    public RecomendacionResult obtenerRecomendacion(UUID usuarioId) {
        RecomendacionCache cached = cache.get(usuarioId);

        if (cached != null && cached.recomendacion != null) {
            resetWindowIfExpired(cached);

            if (cached.generadaEn.plusHours(1).isAfter(LocalDateTime.now())) {
                return buildResult(cached);
            }
        }

        return generarRecomendacion(usuarioId, false);
    }

    public RecomendacionResult regenerarRecomendacion(UUID usuarioId) {
        RecomendacionCache cached = cache.get(usuarioId);

        if (cached != null) {
            resetWindowIfExpired(cached);

            if (cached.manualCount >= MAX_MANUAL_PER_HOUR) {
                return buildResult(cached);
            }
        }

        return generarRecomendacion(usuarioId, true);
    }

    private synchronized RecomendacionResult generarRecomendacion(UUID usuarioId, boolean manual) {
        RecomendacionCache cached = cache.computeIfAbsent(usuarioId, k -> new RecomendacionCache());
        resetWindowIfExpired(cached);

        if (manual) {
            if (cached.manualCount >= MAX_MANUAL_PER_HOUR) {
                return buildResult(cached);
            }
            cached.manualCount++;
        }

        try {
            Balance balance = balanceUseCase.obtenerBalanceGeneral(usuarioId);

            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            LocalDate finMes = LocalDate.now();
            Balance balanceMes = balanceUseCase.obtenerBalancePorPeriodo(usuarioId, inicioMes, finMes);

            Map<CategoriaGasto, BigDecimal> desglose = gastoUseCase.obtenerDesglosePorCategoria(usuarioId);

            List<MetaFinanciera> metasActivas = metaRepository.findByUsuarioIdAndEstado(usuarioId, EstadoMeta.ACTIVA);

            String prompt = buildPrompt(balance, balanceMes, desglose, metasActivas);
            String recomendacion = callOpenAI(prompt);

            cached.recomendacion = recomendacion;
            cached.generadaEn = LocalDateTime.now();

        } catch (Exception e) {
            log.error("Error generando recomendación para usuario {}: {}", usuarioId, e.getMessage());
            if (cached.recomendacion == null) {
                cached.recomendacion = "No fue posible generar una recomendación en este momento. Intenta nuevamente más tarde.";
                cached.generadaEn = LocalDateTime.now();
            }
        }

        return buildResult(cached);
    }

    private void resetWindowIfExpired(RecomendacionCache cached) {
        if (cached.windowStart.plusHours(1).isBefore(LocalDateTime.now())) {
            cached.windowStart = LocalDateTime.now();
            cached.manualCount = 0;
        }
    }

    private RecomendacionResult buildResult(RecomendacionCache cached) {
        int remaining = Math.max(0, MAX_MANUAL_PER_HOUR - cached.manualCount);

        if (cached.windowStart.plusHours(1).isBefore(LocalDateTime.now())) {
            remaining = MAX_MANUAL_PER_HOUR;
        }

        LocalDateTime nextAutoGen = cached.generadaEn != null
                ? cached.generadaEn.plusHours(1)
                : LocalDateTime.now();

        return new RecomendacionResult(
                cached.recomendacion,
                cached.generadaEn,
                remaining,
                nextAutoGen
        );
    }

    private String buildPrompt(Balance balance, Balance balanceMes,
                                Map<CategoriaGasto, BigDecimal> desglose, List<MetaFinanciera> metasActivas) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un asesor financiero personal inteligente. Analiza los siguientes datos financieros del usuario y genera recomendaciones personalizadas, prácticas y accionables. ");
        sb.append("Responde en español, de forma clara y concisa. Usa un tono cercano pero profesional. ");
        sb.append("Genera entre 2 y 4 recomendaciones puntuales. Cada recomendación debe ser específica basada en los datos. ");
        sb.append("Formatea tu respuesta en markdown bien estructurado y visualmente agradable. ");
        sb.append("Usa encabezados (###), **negritas** para destacar cifras o conceptos clave, listas con viñetas, y emojis relevantes. ");
        sb.append("Si los datos muestran buenos hábitos, reconócelos. Si hay oportunidades de mejora, sugiere acciones concretas.\n\n");

        sb.append("=== DATOS FINANCIEROS DEL USUARIO ===\n\n");

        sb.append("📊 Balance General:\n");
        sb.append("- Ingresos totales: $").append(formatMonto(balance.getTotalIngresos())).append("\n");
        sb.append("- Gastos totales: $").append(formatMonto(balance.getTotalGastos())).append("\n");
        sb.append("- Ahorros totales: $").append(formatMonto(balance.getTotalAhorros())).append("\n");
        sb.append("- Deudas pendientes: $").append(formatMonto(balance.getTotalDeudas())).append("\n");
        sb.append("- Dinero disponible: $").append(formatMonto(balance.getDineroDisponible())).append("\n\n");

        sb.append("📅 Balance del Mes Actual:\n");
        sb.append("- Ingresos del mes: $").append(formatMonto(balanceMes.getTotalIngresos())).append("\n");
        sb.append("- Gastos del mes: $").append(formatMonto(balanceMes.getTotalGastos())).append("\n");
        sb.append("- Ahorros del mes: $").append(formatMonto(balanceMes.getTotalAhorros())).append("\n");
        sb.append("- Disponible del mes: $").append(formatMonto(balanceMes.getDineroDisponible())).append("\n\n");

        if (desglose != null && !desglose.isEmpty()) {
            sb.append("📈 Desglose de Gastos por Categoría:\n");
            desglose.forEach((categoria, monto) -> {
                if (monto.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append("- ").append(categoria).append(": $").append(formatMonto(monto)).append("\n");
                }
            });
            sb.append("\n");
        }

        if (metasActivas != null && !metasActivas.isEmpty()) {
            sb.append("🎯 Metas Financieras Activas:\n");
            for (MetaFinanciera meta : metasActivas) {
                sb.append("- ").append(meta.getNombre())
                        .append(": $").append(formatMonto(meta.getMontoActual()))
                        .append(" de $").append(formatMonto(meta.getMontoObjetivo()))
                        .append(" (").append(meta.getPorcentajeAvance()).append("%)")
                        .append("\n");
            }
            sb.append("\n");
        }

        sb.append("Genera tus recomendaciones financieras personalizadas:");

        return sb.toString();
    }

    private String formatMonto(BigDecimal monto) {
        if (monto == null) return "0";
        return String.format("%,.0f", monto);
    }

    @SuppressWarnings("unchecked")
    private String callOpenAI(String prompt) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            return "⚠️ La clave de OpenAI no está configurada. Configura la variable de entorno OPENAI_API_KEY para recibir recomendaciones personalizadas.";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(message),
                "max_tokens", 800,
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> messageResponse = (Map<String, Object>) choices.get(0).get("message");
                    return (String) messageResponse.get("content");
                }
            }
        } catch (Exception e) {
            log.error("Error llamando a OpenAI: {}", e.getMessage());
            throw new RuntimeException("Error al generar recomendación con IA: " + e.getMessage());
        }

        return "No se pudo generar la recomendación. Intenta nuevamente.";
    }

    public record RecomendacionResult(
            String recomendacion,
            LocalDateTime generadaEn,
            int regeneracionesRestantes,
            LocalDateTime proximaRegeneracionAutomatica
    ) {}

    private static class RecomendacionCache {
        String recomendacion;
        LocalDateTime generadaEn;
        int manualCount;
        LocalDateTime windowStart;

        RecomendacionCache() {
            this.windowStart = LocalDateTime.now();
            this.manualCount = 0;
        }
    }
}
