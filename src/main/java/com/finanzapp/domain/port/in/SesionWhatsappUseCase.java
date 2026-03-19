package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.SesionWhatsapp;

import java.util.Optional;

public interface SesionWhatsappUseCase {
    Optional<SesionWhatsapp> verificarSesion(String numeroWhatsapp);
    SesionWhatsapp crearSesion(String numeroWhatsapp);
    SesionWhatsapp renovarSesion(String numeroWhatsapp);
    void cerrarSesion(String numeroWhatsapp);
}
