package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.SesionWhatsapp;
import com.finanzapp.domain.port.out.SesionWhatsappRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.SesionWhatsappMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.SesionWhatsappJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SesionWhatsappRepositoryAdapter implements SesionWhatsappRepositoryPort {

    private final SesionWhatsappJpaRepository repository;
    private final SesionWhatsappMapper mapper;

    @Override
    public SesionWhatsapp save(SesionWhatsapp sesion) {
        var entity = mapper.toEntity(sesion);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SesionWhatsapp> findByNumeroWhatsapp(String numeroWhatsapp) {
        return repository.findByNumeroWhatsapp(numeroWhatsapp).map(mapper::toDomain);
    }

    @Override
    public Optional<SesionWhatsapp> findByNumeroWhatsappAndActivaTrue(String numeroWhatsapp) {
        return repository.findByNumeroWhatsappAndActivaTrue(numeroWhatsapp).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByNumeroWhatsapp(String numeroWhatsapp) {
        repository.deleteByNumeroWhatsapp(numeroWhatsapp);
    }
}
