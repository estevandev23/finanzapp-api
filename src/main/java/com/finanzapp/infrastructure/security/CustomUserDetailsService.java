package com.finanzapp.infrastructure.security;

import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepositoryPort usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return new CustomUserDetails(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.isActivo()
        );
    }
}
