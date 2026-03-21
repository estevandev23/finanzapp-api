package com.finanzapp.infrastructure.adapter.out.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class EvolutionApiService {

    private final String evolutionApiUrl;
    private final String evolutionApiKey;
    private final String instanceName;
    private final RestTemplate restTemplate;

    public EvolutionApiService(
            @Value("${evolution.api.url:http://evolution-api:8080}") String evolutionApiUrl,
            @Value("${evolution.api.key:}") String evolutionApiKey,
            @Value("${evolution.instance.name:finanzapp}") String instanceName) {
        this.evolutionApiUrl = evolutionApiUrl;
        this.evolutionApiKey = evolutionApiKey;
        this.instanceName = instanceName;
        this.restTemplate = new RestTemplate();
    }

    public void enviarMensaje(String numeroWhatsapp, String mensaje) {
        if (evolutionApiKey == null || evolutionApiKey.isBlank()) {
            log.warn("Evolution API key no configurada, omitiendo envio de mensaje WhatsApp");
            return;
        }

        try {
            String url = evolutionApiUrl + "/message/sendText/" + instanceName;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", evolutionApiKey);

            String numero = numeroWhatsapp.startsWith("+") ? numeroWhatsapp.substring(1) : numeroWhatsapp;

            Map<String, Object> body = Map.of(
                    "number", numero,
                    "text", mensaje
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);
            log.info("Mensaje WhatsApp enviado a: {}", numeroWhatsapp);
        } catch (Exception e) {
            log.error("Error al enviar mensaje WhatsApp a {}: {}", numeroWhatsapp, e.getMessage());
        }
    }
}
