package com.finanzapp.domain.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TelefonoUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "3104567890,   +573104567890",
            "573104567890, +573104567890",
            "+573104567890, +573104567890",
            "03104567890,  +573104567890"
    })
    @DisplayName("Debe normalizar numeros colombianos al formato E.164")
    void debeNormalizarNumerosColombianosAlFormatoE164(String entrada, String esperado) {
        assertEquals(esperado, TelefonoUtils.normalizar(entrada));
    }

    @Test
    @DisplayName("Debe retornar null cuando el numero es null")
    void debeRetornarNullCuandoEsNull() {
        assertNull(TelefonoUtils.normalizar(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Debe retornar el valor original cuando esta vacio o en blanco")
    void debeRetornarOriginalCuandoEstaVacioOEnBlanco(String entrada) {
        assertEquals(entrada, TelefonoUtils.normalizar(entrada));
    }

    @Test
    @DisplayName("Debe limpiar caracteres no numericos antes de normalizar")
    void debeLimpiarCaracteresNoNumericos() {
        assertEquals("+573104567890", TelefonoUtils.normalizar("310 456 7890"));
        assertEquals("+573104567890", TelefonoUtils.normalizar("310-456-7890"));
        assertEquals("+573104567890", TelefonoUtils.normalizar("(310) 456-7890"));
    }

    @Test
    @DisplayName("Debe preservar numeros que ya tienen prefijo +")
    void debePreservarNumerosConPrefijoPlus() {
        assertEquals("+573104567890", TelefonoUtils.normalizar("+573104567890"));
        assertEquals("+13105551234", TelefonoUtils.normalizar("+13105551234"));
    }

    @Test
    @DisplayName("Debe manejar numero con codigo de pais 57 sin +")
    void debeManejarNumeroConCodigoPaisSinPlus() {
        assertEquals("+573104567890", TelefonoUtils.normalizar("573104567890"));
    }

    @Test
    @DisplayName("Debe remover cero inicial y agregar +57")
    void debeRemoverCeroInicialYAgregarPrefijo() {
        assertEquals("+573104567890", TelefonoUtils.normalizar("03104567890"));
    }
}
