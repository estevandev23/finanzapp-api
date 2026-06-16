package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.TarjetaCredito;
import com.finanzapp.domain.port.out.TarjetaCreditoRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.TarjetaCreditoEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.TarjetaCreditoMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.TarjetaCreditoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TarjetaCreditoRepositoryAdapter implements TarjetaCreditoRepositoryPort {

    private final TarjetaCreditoJpaRepository tarjetaRepo;
    private final TarjetaCreditoMapper mapper;

    @Override
    public TarjetaCredito save(TarjetaCredito tarjeta) {
        if (tarjeta.getId() != null) {
            Optional<TarjetaCreditoEntity> existente = tarjetaRepo.findById(tarjeta.getId());
            if (existente.isPresent()) {
                TarjetaCreditoEntity entity = existente.get();
                entity.setNombre(tarjeta.getNombre());
                entity.setBanco(tarjeta.getBanco());
                entity.setUltimosCuatro(tarjeta.getUltimosCuatro());
                entity.setCupoTotal(tarjeta.getCupoTotal());
                entity.setCupoUsado(tarjeta.getCupoUsado());
                entity.setDiaCorte(tarjeta.getDiaCorte());
                entity.setDiaPago(tarjeta.getDiaPago());
                entity.setColor(tarjeta.getColor());
                entity.setEstado(tarjeta.getEstado());
                entity.setFechaActualizacion(tarjeta.getFechaActualizacion());
                return mapper.toDomain(tarjetaRepo.save(entity));
            }
        }
        return mapper.toDomain(tarjetaRepo.save(mapper.toEntity(tarjeta)));
    }

    @Override
    public Optional<TarjetaCredito> findById(UUID id) {
        return tarjetaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<TarjetaCredito> findByUsuarioId(UUID usuarioId) {
        return tarjetaRepo.findByUsuarioIdOrderByNombreAsc(usuarioId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        tarjetaRepo.deleteById(id);
    }
}
