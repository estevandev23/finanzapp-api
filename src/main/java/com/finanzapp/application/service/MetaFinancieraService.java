package com.finanzapp.application.service;

import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.Ahorro;
import com.finanzapp.domain.model.EstadoMeta;
import com.finanzapp.domain.model.MetaFinanciera;
import com.finanzapp.domain.port.in.MetaFinancieraUseCase;
import com.finanzapp.domain.port.out.AhorroRepositoryPort;
import com.finanzapp.domain.port.out.MetaFinancieraRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MetaFinancieraService implements MetaFinancieraUseCase {

    private final MetaFinancieraRepositoryPort metaRepository;
    private final AhorroRepositoryPort ahorroRepository;

    @Override
    public MetaFinanciera crear(MetaFinanciera meta) {
        meta.setId(UUID.randomUUID());
        meta.setMontoActual(BigDecimal.ZERO);
        meta.setEstado(EstadoMeta.ACTIVA);
        meta.setFechaCreacion(LocalDateTime.now());
        meta.setFechaActualizacion(LocalDateTime.now());

        return metaRepository.save(meta);
    }

    @Override
    @Transactional(readOnly = true)
    public MetaFinanciera obtenerPorId(UUID id) {
        MetaFinanciera meta = metaRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Meta financiera", id));

        BigDecimal totalAhorros = ahorroRepository.sumMontoByMetaId(id);
        if (totalAhorros != null) {
            meta.setMontoActual(totalAhorros);
        }

        return meta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanciera> listarPorUsuario(UUID usuarioId) {
        List<MetaFinanciera> metas = metaRepository.findByUsuarioId(usuarioId);
        actualizarMontosActuales(metas);
        return metas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanciera> listarPorEstado(UUID usuarioId, EstadoMeta estado) {
        List<MetaFinanciera> metas = metaRepository.findByUsuarioIdAndEstado(usuarioId, estado);
        actualizarMontosActuales(metas);
        return metas;
    }

    private void actualizarMontosActuales(List<MetaFinanciera> metas) {
        for (MetaFinanciera meta : metas) {
            BigDecimal totalAhorros = ahorroRepository.sumMontoByMetaId(meta.getId());
            if (totalAhorros != null) {
                meta.setMontoActual(totalAhorros);
            }
        }
    }

    @Override
    public MetaFinanciera actualizar(UUID id, MetaFinanciera metaActualizada) {
        MetaFinanciera meta = obtenerPorId(id);

        if (metaActualizada.getNombre() != null) {
            meta.setNombre(metaActualizada.getNombre());
        }
        if (metaActualizada.getDescripcion() != null) {
            meta.setDescripcion(metaActualizada.getDescripcion());
        }
        if (metaActualizada.getMontoObjetivo() != null) {
            meta.setMontoObjetivo(metaActualizada.getMontoObjetivo());
        }
        if (metaActualizada.getFechaLimite() != null) {
            meta.setFechaLimite(metaActualizada.getFechaLimite());
        }

        meta.setFechaActualizacion(LocalDateTime.now());
        return metaRepository.save(meta);
    }

    @Override
    public MetaFinanciera registrarProgreso(UUID metaId, UUID usuarioId, BigDecimal monto, String descripcion) {
        MetaFinanciera meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new RecursoNotFoundException("Meta financiera", metaId));

        Ahorro ahorro = Ahorro.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .metaId(metaId)
                .monto(monto)
                .descripcion(descripcion != null ? descripcion : "Abono a meta: " + meta.getNombre())
                .fecha(LocalDate.now())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        ahorroRepository.save(ahorro);

        BigDecimal totalAhorros = ahorroRepository.sumMontoByMetaId(metaId);
        meta.setMontoActual(totalAhorros != null ? totalAhorros : BigDecimal.ZERO);

        if (meta.isCompletada()) {
            meta.setEstado(EstadoMeta.COMPLETADA);
        }

        meta.setFechaActualizacion(LocalDateTime.now());
        return metaRepository.save(meta);
    }

    @Override
    public MetaFinanciera cambiarEstado(UUID metaId, EstadoMeta nuevoEstado) {
        MetaFinanciera meta = obtenerPorId(metaId);
        meta.setEstado(nuevoEstado);
        meta.setFechaActualizacion(LocalDateTime.now());
        return metaRepository.save(meta);
    }

    @Override
    public void eliminar(UUID id) {
        obtenerPorId(id);
        metaRepository.deleteById(id);
    }
}
