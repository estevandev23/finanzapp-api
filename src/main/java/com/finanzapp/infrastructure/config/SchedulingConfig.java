package com.finanzapp.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita el procesamiento de métodos @Scheduled (jobs en background).
 * Necesario para recordatorios de recurrencias, alertas de presupuesto, etc.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
