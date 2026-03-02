package com.finanzapp.application.service;

import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.CategoriaPersonalizada;
import com.finanzapp.domain.model.TipoCategoria;
import com.finanzapp.domain.port.in.CategoriaPersonalizadaUseCase;
import com.finanzapp.domain.port.out.CategoriaPersonalizadaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaPersonalizadaService implements CategoriaPersonalizadaUseCase {

    private final CategoriaPersonalizadaRepositoryPort categoriaRepository;

    @Override
    public CategoriaPersonalizada crear(CategoriaPersonalizada categoria) {
        if (categoriaRepository.existsByUsuarioIdAndNombreAndTipo(
                categoria.getUsuarioId(), categoria.getNombre(), categoria.getTipo())) {
            throw new DomainException("Ya existe una categoría con este nombre para el tipo seleccionado");
        }

        categoria.setId(UUID.randomUUID());
        categoria.setActiva(true);
        categoria.setFechaCreacion(LocalDateTime.now());
        categoria.setFechaActualizacion(LocalDateTime.now());

        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaPersonalizada obtenerPorId(UUID id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Categoría personalizada", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaPersonalizada> listarPorUsuario(UUID usuarioId) {
        return categoriaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaPersonalizada> listarPorUsuarioYTipo(UUID usuarioId, TipoCategoria tipo) {
        return categoriaRepository.findByUsuarioIdAndTipo(usuarioId, tipo);
    }

    @Override
    public CategoriaPersonalizada actualizar(UUID id, CategoriaPersonalizada categoriaActualizada) {
        CategoriaPersonalizada categoria = obtenerPorId(id);

        if (categoriaActualizada.getNombre() != null) {
            categoria.setNombre(categoriaActualizada.getNombre());
        }
        if (categoriaActualizada.getColor() != null) {
            categoria.setColor(categoriaActualizada.getColor());
        }
        if (categoriaActualizada.getIcono() != null) {
            categoria.setIcono(categoriaActualizada.getIcono());
        }

        categoria.setFechaActualizacion(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    @Override
    public void eliminar(UUID id) {
        obtenerPorId(id);
        categoriaRepository.deleteById(id);
    }
}
