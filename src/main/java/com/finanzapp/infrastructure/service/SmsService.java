package com.finanzapp.infrastructure.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.phone-number:}")
    private String fromPhoneNumber;

    private boolean configurado = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            configurado = true;
            log.info("Twilio SMS inicializado correctamente");
        } else {
            log.warn("Twilio SMS no configurado. Las funciones de SMS no estarán disponibles.");
        }
    }

    public void enviarCodigoVerificacion(String destinatario, String codigo) {
        if (!configurado) {
            log.error("Twilio no está configurado. No se puede enviar SMS.");
            throw new RuntimeException("El servicio de SMS no está configurado");
        }

        try {
            String cuerpoMensaje = String.format(
                    "Tu codigo de verificacion de FinanzApp es: %s. Expira en 10 minutos.", codigo);

            // El destinatario puede venir en formato local; se normaliza a E.164
            String numeroDestinatario = formatearNumeroDestinatario(destinatario);

            // El número remitente de Twilio debe estar ya en E.164 en la configuración
            Message message = Message.creator(
                    new PhoneNumber(numeroDestinatario),
                    new PhoneNumber(fromPhoneNumber),
                    cuerpoMensaje
            ).create();

            log.info("SMS enviado a {}. SID: {}", destinatario, message.getSid());
        } catch (Exception e) {
            log.error("Error al enviar SMS a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar el SMS de verificación");
        }
    }

    /**
     * Normaliza el número del destinatario a formato E.164.
     * El número remitente (fromPhoneNumber) de Twilio debe configurarse
     * directamente en E.164 en application.yml o la variable de entorno TWILIO_PHONE_NUMBER.
     */
    private String formatearNumeroDestinatario(String numero) {
        String limpio = numero.replaceAll("[^0-9+]", "");

        if (limpio.startsWith("+")) {
            return limpio;
        }
        if (limpio.startsWith("57")) {
            return "+" + limpio;
        }
        return "+57" + limpio;
    }
}
