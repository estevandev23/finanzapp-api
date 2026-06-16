package com.finanzapp.application.service;

import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoRecurrencia;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.domain.port.out.RecurrenciaRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.messaging.EvolutionApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Job diario que envía un recordatorio por WhatsApp para cada recurrencia activa cuya
 * próxima fecha esté dentro de los próximos 3 días o ya esté vencida.
 *
 * No genera ingresos ni gastos automáticamente: la confirmación siempre es manual
 * por parte del usuario.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecurrenciaRecordatorioJob {

    private static final int DIAS_ANTICIPACION = 3;
    private static final NumberFormat FORMATEADOR_COP = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final RecurrenciaRepositoryPort recurrenciaRepository;
    private final DispositivoRepositoryPort dispositivoRepository;
    private final EvolutionApiService evolutionApiService;

    /** Se ejecuta diariamente a las 9:00 AM hora local del servidor. */
    @Scheduled(cron = "0 0 9 * * *")
    public void enviarRecordatoriosDiarios() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(DIAS_ANTICIPACION);

        List<Recurrencia> pendientes = recurrenciaRepository
                .findActivasEnRangoProximaFecha(hoy.minusDays(30), limite);

        if (pendientes.isEmpty()) {
            log.debug("No hay recurrencias pendientes de recordatorio");
            return;
        }

        log.info("Enviando recordatorios para {} recurrencias", pendientes.size());

        for (Recurrencia recurrencia : pendientes) {
            try {
                notificarRecurrencia(recurrencia, hoy);
            } catch (Exception e) {
                log.error("Error procesando recordatorio de recurrencia {}: {}",
                        recurrencia.getId(), e.getMessage());
            }
        }
    }

    private void notificarRecurrencia(Recurrencia recurrencia, LocalDate hoy) {
        List<Dispositivo> dispositivos = dispositivoRepository
                .findByUsuarioIdAndActivo(recurrencia.getUsuarioId(), true);

        if (dispositivos.isEmpty()) {
            log.debug("Usuario {} no tiene dispositivos WhatsApp activos", recurrencia.getUsuarioId());
            return;
        }

        String mensaje = construirMensaje(recurrencia, hoy);

        for (Dispositivo dispositivo : dispositivos) {
            if (!dispositivo.isVerificado()) continue;
            evolutionApiService.enviarMensaje(dispositivo.getNumeroWhatsapp(), mensaje);
        }
    }

    private String construirMensaje(Recurrencia recurrencia, LocalDate hoy) {
        long dias = recurrencia.diasParaVencer(hoy);
        String tipoLabel = recurrencia.getTipo() == TipoRecurrencia.INGRESO ? "ingreso" : "gasto";
        String monto = FORMATEADOR_COP.format(recurrencia.getMonto());

        if (dias < 0) {
            return String.format(
                    "⏰ Recordatorio FinanzApp\n\n" +
                    "Tu %s recurrente \"%s\" por %s está vencido desde hace %d día(s).\n" +
                    "Si ya lo recibiste/pagaste, confírmalo respondiendo \"confirmar %s\".",
                    tipoLabel, recurrencia.getDescripcion(), monto, Math.abs(dias),
                    recurrencia.getDescripcion());
        }
        if (dias == 0) {
            return String.format(
                    "⏰ Recordatorio FinanzApp\n\n" +
                    "Hoy vence tu %s recurrente \"%s\" por %s.\n" +
                    "Confírmalo cuando lo realices.",
                    tipoLabel, recurrencia.getDescripcion(), monto);
        }
        return String.format(
                "📅 Recordatorio FinanzApp\n\n" +
                "En %d día(s) vence tu %s recurrente \"%s\" por %s.",
                dias, tipoLabel, recurrencia.getDescripcion(), monto);
    }
}
