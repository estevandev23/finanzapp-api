package com.finanzapp.domain.util;

/**
 * Utilidad para normalizar numeros de telefono al formato E.164 colombiano (+57XXXXXXXXXX).
 */
public final class TelefonoUtils {

    private static final String CODIGO_PAIS_COLOMBIA = "57";
    private static final String PREFIJO_INTERNACIONAL = "+";
    private static final int LONGITUD_NUMERO_LOCAL = 10;
    private static final int LONGITUD_E164_COLOMBIA = 12;

    private TelefonoUtils() {
    }

    public static String normalizar(String numero) {
        if (numero == null || numero.isBlank()) {
            return numero;
        }

        String limpio = numero.replaceAll("[^0-9+]", "");

        if (limpio.startsWith(PREFIJO_INTERNACIONAL)) {
            return limpio;
        }

        if (limpio.startsWith(CODIGO_PAIS_COLOMBIA) && limpio.length() >= LONGITUD_E164_COLOMBIA) {
            return PREFIJO_INTERNACIONAL + limpio;
        }

        if (limpio.startsWith("0")) {
            limpio = limpio.substring(1);
        }

        if (limpio.startsWith("3") && limpio.length() == LONGITUD_NUMERO_LOCAL) {
            return PREFIJO_INTERNACIONAL + CODIGO_PAIS_COLOMBIA + limpio;
        }

        return PREFIJO_INTERNACIONAL + limpio;
    }
}
