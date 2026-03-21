package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.WhatsappLoginToken;
import com.finanzapp.domain.port.out.WhatsappLoginTokenRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.WhatsappLoginTokenMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.WhatsappLoginTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WhatsappLoginTokenRepositoryAdapter implements WhatsappLoginTokenRepositoryPort {

    private final WhatsappLoginTokenJpaRepository repository;
    private final WhatsappLoginTokenMapper mapper;

    @Override
    public WhatsappLoginToken save(WhatsappLoginToken token) {
        var entity = mapper.toEntity(token);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<WhatsappLoginToken> findByToken(String token) {
        return repository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteExpired() {
        repository.deleteExpired(LocalDateTime.now());
    }
}
