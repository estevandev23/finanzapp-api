package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.SesionWhatsapp;

import java.util.Optional;

public interface SesionWhatsappRepositoryPort {
    SesionWhatsapp save(SesionWhatsapp sesion);
    Optional<SesionWhatsapp> findByNumeroWhatsapp(String numeroWhatsapp);
    Optional<SesionWhatsapp> findByNumeroWhatsappAndActivaTrue(String numeroWhatsapp);
    void deleteByNumeroWhatsapp(String numeroWhatsapp);
}
