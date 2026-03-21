package com.finanzapp.domain.port.out;

import com.finanzapp.domain.model.WhatsappLoginToken;

import java.util.Optional;

public interface WhatsappLoginTokenRepositoryPort {
    WhatsappLoginToken save(WhatsappLoginToken token);
    Optional<WhatsappLoginToken> findByToken(String token);
    void deleteExpired();
}
