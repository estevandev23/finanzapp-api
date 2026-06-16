package com.finanzapp.application.service;

import com.finanzapp.domain.model.AlertaPresupuestoEmitida;
import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.model.EstadoBolsilloMensual;
import com.finanzapp.domain.model.EstadoPresupuestoMensual;
import com.finanzapp.domain.model.NivelAlertaPresupuesto;
import com.finanzapp.domain.model.PresupuestoMensual;
import com.finanzapp.domain.model.PresupuestoPlantilla;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.domain.port.out.PresupuestoRepositoryPort;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import com.finanzapp.infrastructure.service.EmailService;
import com.finanzapp.infrastructure.adapter.out.messaging.EvolutionApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Job que recorre cada usuario con plantilla activa, calcula el estado del mes actual
 * y emite alertas (ADVERTENCIA al 80%, EXCEDIDO al 100%) por WhatsApp + Email solo
 * la primera vez que el bolsillo cruza cada umbral.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertaPresupuestoJob {

    private final PresupuestoRepositoryPort presupuestoRepository;
    private final PresupuestoService presupuestoService;
    private final DispositivoRepositoryPort dispositivoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final EvolutionApiService evolutionApiService;
    private final EmailService emailService;

    private static final NumberFormat COP = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    /** Diario a las 09:00. */
    @Scheduled(cron = "0 0 9 * * *")
    public void verificarAlertasPresupuesto() {
        LocalDate hoy = LocalDate.now();
        int anio = hoy.getYear();
        int mes = hoy.getMonthValue();

        List<PresupuestoPlantilla> plantillas = presupuestoRepository.findAllPlantillas();
        log.info("AlertaPresupuestoJob: revisando {} plantillas para {}/{}", plantillas.size(), mes, anio);

        for (PresupuestoPlantilla plantilla : plantillas) {
            try {
                procesarUsuario(plantilla.getUsuarioId(), anio, mes);
            } catch (Exception e) {
                log.error("Error procesando alertas para usuario {}: {}",
                        plantilla.getUsuarioId(), e.getMessage(), e);
            }
        }
    }

    private void procesarUsuario(UUID usuarioId, int anio, int mes) {
        EstadoPresupuestoMensual estado;
        try {
            estado = presupuestoService.obtenerEstadoMensual(usuarioId, anio, mes);
        } catch (Exception e) {
            log.debug("Usuario {} sin presupuesto procesable: {}", usuarioId, e.getMessage());
            return;
        }

        if (estado.getBolsillos() == null) return;

        for (EstadoBolsilloMensual eb : estado.getBolsillos()) {
            if (eb.getNivel() == NivelAlertaPresupuesto.OK) continue;

            UUID bolsilloMensualId = eb.getBolsillo().getId();
            NivelAlertaPresupuesto nivel = eb.getNivel();

            if (presupuestoRepository.existsAlerta(bolsilloMensualId, nivel)) {
                continue;
            }

            enviarAlerta(usuarioId, eb);

            presupuestoRepository.saveAlerta(AlertaPresupuestoEmitida.builder()
                    .id(UUID.randomUUID())
                    .bolsilloMensualId(bolsilloMensualId)
                    .nivel(nivel)
                    .fechaEmision(LocalDateTime.now())
                    .build());
        }
    }

    private void enviarAlerta(UUID usuarioId, EstadoBolsilloMensual eb) {
        String nombreBolsillo = eb.getBolsillo().getNombre();
        String porcentaje = eb.getPorcentajeUso().toPlainString();
        String gastado = COP.format(eb.getMontoGastado());
        String limite = COP.format(eb.getBolsillo().getMontoLimite());

        String titulo = eb.getNivel() == NivelAlertaPresupuesto.EXCEDIDO
                ? "Bolsillo excedido"
                : "Bolsillo cerca del límite";
        String emoji = eb.getNivel() == NivelAlertaPresupuesto.EXCEDIDO ? "[!]" : "[?]";

        String mensajeWhats = String.format(
                "%s %s\n" +
                "El bolsillo \"%s\" va en %s%% de su límite.\n" +
                "Gastado: %s de %s.",
                emoji, titulo, nombreBolsillo, porcentaje, gastado, limite);

        // WhatsApp a todos los dispositivos verificados
        List<Dispositivo> dispositivos = dispositivoRepository.findByUsuarioIdAndActivo(usuarioId, true);
        for (Dispositivo d : dispositivos) {
            if (!d.isVerificado()) continue;
            try {
                evolutionApiService.enviarMensaje(d.getNumeroWhatsapp(), mensajeWhats);
            } catch (Exception e) {
                log.warn("No se pudo enviar WhatsApp de alerta a {}: {}",
                        d.getNumeroWhatsapp(), e.getMessage());
            }
        }

        // Email
        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
        usuario.ifPresent(u -> {
            if (u.getEmail() == null || u.getEmail().isBlank()) return;
            try {
                String asunto = "FinanzApp - " + titulo + ": " + nombreBolsillo;
                String cuerpo = String.format(
                        "Hola,%n%n" +
                        "Te avisamos que tu bolsillo \"%s\" del presupuesto va en %s%% de su límite.%n" +
                        "Gastado: %s%nLímite: %s%n%n" +
                        "Revisa tus movimientos en FinanzApp.",
                        nombreBolsillo, porcentaje, gastado, limite);
                emailService.enviarNotificacion(u.getEmail(), asunto, cuerpo);
            } catch (Exception e) {
                log.warn("No se pudo enviar email de alerta a {}: {}", u.getEmail(), e.getMessage());
            }
        });
    }
}
