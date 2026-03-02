package com.finanzapp.domain.port.in;

import com.finanzapp.domain.model.Usuario;

import java.util.UUID;

public interface AuthUseCase {
    String login(String email, String password);
    Usuario authenticateUser(String email, String password);
    String loginWhatsapp(String numeroWhatsapp, String codigoVerificacion);
    Usuario loginOAuth(String provider, String providerId, String email, String nombre);
    Usuario registrar(Usuario usuario);
    void logout(String token);
    String refreshToken(String token);
    void cambiarPassword(UUID usuarioId, String nuevaPassword);
}
