package com.finanzapp.application.service;

import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.exception.UsuarioNotFoundException;
import com.finanzapp.domain.exception.UsuarioYaExisteException;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.in.UsuarioUseCase;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService implements UsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new UsuarioYaExisteException(usuario.getEmail());
        }

        usuario.setId(UUID.randomUUID());
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));
    }

    @Override
    public Usuario actualizar(UUID id, Usuario usuarioActualizado) {
        Usuario usuario = obtenerPorId(id);

        if (usuarioActualizado.getNombre() != null) {
            usuario.setNombre(usuarioActualizado.getNombre());
        }
        if (usuarioActualizado.getTelefono() != null) {
            boolean telefonoCambiado = !usuarioActualizado.getTelefono().equals(usuario.getTelefono());
            if (telefonoCambiado && usuarioRepository.existsByTelefono(usuarioActualizado.getTelefono())) {
                throw new DomainException("Ya existe un usuario con este número de teléfono");
            }
            usuario.setTelefono(usuarioActualizado.getTelefono());
        }

        usuario.setFechaActualizacion(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    @Override
    public void eliminar(UUID id) {
        Usuario usuario = obtenerPorId(id);
        usuario.setActivo(false);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Override
    public void cambiarPassword(UUID id, String passwordActual, String nuevaPassword) {
        Usuario usuario = obtenerPorId(id);

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new DomainException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }
}
