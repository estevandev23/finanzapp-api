package com.finanzapp.infrastructure.adapter.in.rest.controller;

import com.finanzapp.application.service.SesionWhatsappService;
import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.model.Dispositivo;
import com.finanzapp.domain.model.SesionWhatsapp;
import com.finanzapp.domain.model.Usuario;
import com.finanzapp.domain.model.WhatsappLoginToken;
import com.finanzapp.domain.port.in.DispositivoUseCase;
import com.finanzapp.domain.port.out.DispositivoRepositoryPort;
import com.finanzapp.domain.port.out.UsuarioRepositoryPort;
import com.finanzapp.domain.port.out.WhatsappLoginTokenRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.messaging.EvolutionApiService;
import com.finanzapp.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WhatsappAuthController")
class WhatsappAuthControllerTest {

    @Mock
    private SesionWhatsappService sesionWhatsappService;
    @Mock
    private DispositivoUseCase dispositivoUseCase;
    @Mock
    private DispositivoRepositoryPort dispositivoRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WhatsappLoginTokenRepositoryPort whatsappLoginTokenRepository;
    @Mock
    private EvolutionApiService evolutionApiService;
    @Mock
    private JwtService jwtService;

    private WhatsappAuthController controller;

    private static final String FRONTEND_URL = "https://finanzappweb.estevanv.dev";
    private static final String NUMERO_WHATSAPP = "3104567890";
    private static final String NUMERO_NORMALIZADO = "+573104567890";

    @BeforeEach
    void setUp() {
        controller = new WhatsappAuthController(
                sesionWhatsappService,
                dispositivoUseCase,
                dispositivoRepository,
                usuarioRepository,
                passwordEncoder,
                whatsappLoginTokenRepository,
                evolutionApiService,
                jwtService,
                FRONTEND_URL
        );
    }

    @Nested
    @DisplayName("generarLinkOAuth")
    class GenerarLinkOAuthTests {

        @Test
        @DisplayName("Debe generar un token y retornar URL del frontend")
        void debeGenerarTokenYRetornarUrl() {
            when(whatsappLoginTokenRepository.save(any(WhatsappLoginToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var response = controller.generarLinkOAuth(NUMERO_WHATSAPP);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
            String url = (String) data.get("url");
            assertNotNull(url);
            assertTrue(url.startsWith(FRONTEND_URL + "/whatsapp-login?token="));
            assertEquals(30, data.get("expiraEnMinutos"));
        }

        @Test
        @DisplayName("Debe guardar el token con expiracion de 30 minutos")
        void debeGuardarTokenConExpiracion30Min() {
            ArgumentCaptor<WhatsappLoginToken> captor = ArgumentCaptor.forClass(WhatsappLoginToken.class);
            when(whatsappLoginTokenRepository.save(captor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            controller.generarLinkOAuth(NUMERO_WHATSAPP);

            WhatsappLoginToken tokenGuardado = captor.getValue();
            assertNotNull(tokenGuardado.getId());
            assertNotNull(tokenGuardado.getToken());
            assertEquals(NUMERO_NORMALIZADO, tokenGuardado.getNumeroWhatsapp());
            assertFalse(tokenGuardado.isUsado());
            assertNotNull(tokenGuardado.getFechaCreacion());
            assertNotNull(tokenGuardado.getFechaExpiracion());

            long minutosExpiracion = java.time.Duration.between(
                    tokenGuardado.getFechaCreacion(),
                    tokenGuardado.getFechaExpiracion()
            ).toMinutes();
            assertEquals(30, minutosExpiracion);
        }

        @Test
        @DisplayName("Debe normalizar el numero de telefono")
        void debeNormalizarNumero() {
            ArgumentCaptor<WhatsappLoginToken> captor = ArgumentCaptor.forClass(WhatsappLoginToken.class);
            when(whatsappLoginTokenRepository.save(captor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            controller.generarLinkOAuth("573104567890");

            assertEquals(NUMERO_NORMALIZADO, captor.getValue().getNumeroWhatsapp());
        }

        @Test
        @DisplayName("Debe generar tokens unicos en cada invocacion")
        void debeGenerarTokensUnicos() {
            when(whatsappLoginTokenRepository.save(any(WhatsappLoginToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var response1 = controller.generarLinkOAuth(NUMERO_WHATSAPP);
            var response2 = controller.generarLinkOAuth(NUMERO_WHATSAPP);

            @SuppressWarnings("unchecked")
            String url1 = (String) ((Map<String, Object>) response1.getBody().getData()).get("url");
            @SuppressWarnings("unchecked")
            String url2 = (String) ((Map<String, Object>) response2.getBody().getData()).get("url");

            assertNotEquals(url1, url2);
        }
    }

    @Nested
    @DisplayName("validarToken")
    class ValidarTokenTests {

        @Test
        @DisplayName("Debe retornar valido para token existente y no expirado")
        void debeRetornarValidoParaTokenExistente() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken token = crearTokenValido(tokenStr);
            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

            var response = controller.validarToken(tokenStr);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().getData();
            assertTrue((Boolean) data.get("valido"));
        }

        @Test
        @DisplayName("Debe retornar error para token inexistente")
        void debeRetornarErrorParaTokenInexistente() {
            when(whatsappLoginTokenRepository.findByToken("token-invalido")).thenReturn(Optional.empty());

            var response = controller.validarToken("token-invalido");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse(response.getBody().isSuccess());
            assertTrue(response.getBody().getMessage().contains("no es valido"));
        }

        @Test
        @DisplayName("Debe retornar error para token usado")
        void debeRetornarErrorParaTokenUsado() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken token = crearTokenValido(tokenStr);
            token.setUsado(true);
            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

            var response = controller.validarToken(tokenStr);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse(response.getBody().isSuccess());
            assertTrue(response.getBody().getMessage().contains("ya fue utilizado"));
        }

        @Test
        @DisplayName("Debe retornar error para token expirado")
        void debeRetornarErrorParaTokenExpirado() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken token = crearTokenExpirado(tokenStr);
            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

            var response = controller.validarToken(tokenStr);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse(response.getBody().isSuccess());
            assertTrue(response.getBody().getMessage().contains("expirado"));
        }
    }

    @Nested
    @DisplayName("confirmarOAuth")
    class ConfirmarOAuthTests {

        private final UUID userId = UUID.randomUUID();
        private final String jwtToken = "valid-jwt-token";
        private final String authHeader = "Bearer " + jwtToken;

        @Test
        @DisplayName("Debe vincular cuenta exitosamente con token valido")
        void debeVincularCuentaExitosamente() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken loginToken = crearTokenValido(tokenStr);

            Usuario usuario = crearUsuario(userId);

            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(loginToken));
            when(jwtService.extractUserId(jwtToken)).thenReturn(userId);
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(dispositivoRepository.findByNumeroWhatsapp(NUMERO_NORMALIZADO)).thenReturn(Optional.empty());
            when(dispositivoRepository.save(any(Dispositivo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(sesionWhatsappService.crearSesion(NUMERO_NORMALIZADO)).thenReturn(mock(SesionWhatsapp.class));
            when(whatsappLoginTokenRepository.save(any(WhatsappLoginToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = controller.confirmarOAuth(tokenStr, authHeader);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            verify(sesionWhatsappService).crearSesion(NUMERO_NORMALIZADO);
            verify(evolutionApiService).enviarMensaje(eq(NUMERO_NORMALIZADO), anyString());
        }

        @Test
        @DisplayName("Debe marcar token como usado tras confirmar")
        void debeMarcarTokenComoUsado() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken loginToken = crearTokenValido(tokenStr);
            Usuario usuario = crearUsuario(userId);

            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(loginToken));
            when(jwtService.extractUserId(jwtToken)).thenReturn(userId);
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(dispositivoRepository.findByNumeroWhatsapp(NUMERO_NORMALIZADO)).thenReturn(Optional.empty());
            when(dispositivoRepository.save(any(Dispositivo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(sesionWhatsappService.crearSesion(NUMERO_NORMALIZADO)).thenReturn(mock(SesionWhatsapp.class));

            ArgumentCaptor<WhatsappLoginToken> captor = ArgumentCaptor.forClass(WhatsappLoginToken.class);
            when(whatsappLoginTokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            controller.confirmarOAuth(tokenStr, authHeader);

            assertTrue(captor.getValue().isUsado());
        }

        @Test
        @DisplayName("Debe lanzar excepcion con token inexistente")
        void debeLanzarExcepcionConTokenInexistente() {
            when(whatsappLoginTokenRepository.findByToken("no-existe")).thenReturn(Optional.empty());

            DomainException exception = assertThrows(DomainException.class,
                    () -> controller.confirmarOAuth("no-existe", authHeader));

            assertTrue(exception.getMessage().contains("invalido o expirado"));
        }

        @Test
        @DisplayName("Debe lanzar excepcion con token ya usado")
        void debeLanzarExcepcionConTokenUsado() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken loginToken = crearTokenValido(tokenStr);
            loginToken.setUsado(true);

            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(loginToken));

            DomainException exception = assertThrows(DomainException.class,
                    () -> controller.confirmarOAuth(tokenStr, authHeader));

            assertTrue(exception.getMessage().contains("ya fue utilizado"));
        }

        @Test
        @DisplayName("Debe lanzar excepcion con token expirado")
        void debeLanzarExcepcionConTokenExpirado() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken loginToken = crearTokenExpirado(tokenStr);

            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(loginToken));

            DomainException exception = assertThrows(DomainException.class,
                    () -> controller.confirmarOAuth(tokenStr, authHeader));

            assertTrue(exception.getMessage().contains("expirado"));
        }

        @Test
        @DisplayName("Debe lanzar excepcion con JWT invalido (userId null)")
        void debeLanzarExcepcionConJwtInvalido() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken loginToken = crearTokenValido(tokenStr);

            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(loginToken));
            when(jwtService.extractUserId(jwtToken)).thenReturn(null);

            DomainException exception = assertThrows(DomainException.class,
                    () -> controller.confirmarOAuth(tokenStr, authHeader));

            assertTrue(exception.getMessage().contains("autenticacion invalido"));
        }

        @Test
        @DisplayName("Debe vincular telefono al usuario si no tiene uno")
        void debeVincularTelefonoAlUsuario() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken loginToken = crearTokenValido(tokenStr);
            Usuario usuario = crearUsuario(userId);
            usuario.setTelefono(null);

            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(loginToken));
            when(jwtService.extractUserId(jwtToken)).thenReturn(userId);
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(dispositivoRepository.findByNumeroWhatsapp(NUMERO_NORMALIZADO)).thenReturn(Optional.empty());
            when(dispositivoRepository.save(any(Dispositivo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(sesionWhatsappService.crearSesion(NUMERO_NORMALIZADO)).thenReturn(mock(SesionWhatsapp.class));
            when(whatsappLoginTokenRepository.save(any(WhatsappLoginToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            controller.confirmarOAuth(tokenStr, authHeader);

            assertEquals(NUMERO_NORMALIZADO, usuario.getTelefono());
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Debe reutilizar dispositivo existente en lugar de crear uno nuevo")
        void debeReutilizarDispositivoExistente() {
            String tokenStr = UUID.randomUUID().toString();
            WhatsappLoginToken loginToken = crearTokenValido(tokenStr);
            Usuario usuario = crearUsuario(userId);
            Dispositivo dispositivoExistente = Dispositivo.builder()
                    .id(UUID.randomUUID())
                    .usuarioId(UUID.randomUUID())
                    .numeroWhatsapp(NUMERO_NORMALIZADO)
                    .build();

            when(whatsappLoginTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(loginToken));
            when(jwtService.extractUserId(jwtToken)).thenReturn(userId);
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(dispositivoRepository.findByNumeroWhatsapp(NUMERO_NORMALIZADO))
                    .thenReturn(Optional.of(dispositivoExistente));
            when(dispositivoRepository.save(any(Dispositivo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(sesionWhatsappService.crearSesion(NUMERO_NORMALIZADO)).thenReturn(mock(SesionWhatsapp.class));
            when(whatsappLoginTokenRepository.save(any(WhatsappLoginToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            controller.confirmarOAuth(tokenStr, authHeader);

            ArgumentCaptor<Dispositivo> captor = ArgumentCaptor.forClass(Dispositivo.class);
            verify(dispositivoRepository).save(captor.capture());
            assertEquals(userId, captor.getValue().getUsuarioId());
            assertTrue(captor.getValue().isActivo());
            assertTrue(captor.getValue().isVerificado());
        }
    }

    @Nested
    @DisplayName("verificarEstado")
    class VerificarEstadoTests {

        @Test
        @DisplayName("Debe indicar sesion activa cuando existe")
        void debeIndicarSesionActiva() {
            SesionWhatsapp sesion = SesionWhatsapp.builder()
                    .id(UUID.randomUUID())
                    .numeroWhatsapp(NUMERO_NORMALIZADO)
                    .activa(true)
                    .build();

            when(sesionWhatsappService.verificarSesion(NUMERO_NORMALIZADO)).thenReturn(Optional.of(sesion));
            when(dispositivoRepository.findByNumeroWhatsapp(NUMERO_NORMALIZADO)).thenReturn(Optional.empty());

            var response = controller.verificarEstado(NUMERO_WHATSAPP);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().getData().isSesionActiva());
        }

        @Test
        @DisplayName("Debe indicar sesion inactiva sin sesion")
        void debeIndicarSesionInactivaSinSesion() {
            when(sesionWhatsappService.verificarSesion(NUMERO_NORMALIZADO)).thenReturn(Optional.empty());
            when(sesionWhatsappService.renovarSesion(NUMERO_NORMALIZADO)).thenThrow(new RuntimeException("No hay sesion para renovar"));
            when(dispositivoRepository.findByNumeroWhatsapp(NUMERO_NORMALIZADO)).thenReturn(Optional.empty());

            var response = controller.verificarEstado(NUMERO_WHATSAPP);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertFalse(response.getBody().getData().isSesionActiva());
            assertFalse(response.getBody().getData().isCuentaExiste());
        }
    }

    // Metodos auxiliares para crear entidades de prueba

    private WhatsappLoginToken crearTokenValido(String tokenStr) {
        return WhatsappLoginToken.builder()
                .id(UUID.randomUUID())
                .token(tokenStr)
                .numeroWhatsapp(NUMERO_NORMALIZADO)
                .usado(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    private WhatsappLoginToken crearTokenExpirado(String tokenStr) {
        return WhatsappLoginToken.builder()
                .id(UUID.randomUUID())
                .token(tokenStr)
                .numeroWhatsapp(NUMERO_NORMALIZADO)
                .usado(false)
                .fechaCreacion(LocalDateTime.now().minusMinutes(60))
                .fechaExpiracion(LocalDateTime.now().minusMinutes(30))
                .build();
    }

    private Usuario crearUsuario(UUID userId) {
        return Usuario.builder()
                .id(userId)
                .nombre("Test User")
                .email("test@example.com")
                .telefono(NUMERO_NORMALIZADO)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }
}
