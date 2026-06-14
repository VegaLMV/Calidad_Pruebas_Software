package com.kantus.authservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kantus.authservice.dto.request.LoginRequest;
import com.kantus.authservice.dto.response.AuthResponse;
import com.kantus.authservice.entity.AuthAuditLog;
import com.kantus.authservice.entity.Permiso;
import com.kantus.authservice.entity.Rol;
import com.kantus.authservice.entity.RolPermiso;
import com.kantus.authservice.entity.Sesion;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.entity.UsuarioRol;
import com.kantus.authservice.exception.CredencialesInvalidasException;
import com.kantus.authservice.exception.UsuarioBloqueadoException;
import com.kantus.authservice.repository.AuthAuditLogRepository;
import com.kantus.authservice.repository.RolPermisoRepository;
import com.kantus.authservice.repository.SesionRepository;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.repository.UsuarioRolRepository;
import com.kantus.authservice.security.CustomUserDetailsService;
import com.kantus.authservice.security.JwtService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Pruebas unitarias de AuthService usando JUnit 5 y Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private AuthAuditLogRepository auditLogRepository;

  @Mock
  private SesionRepository sesionRepository;

  @Mock
  private CustomUserDetailsService userDetailsService;

  @Mock
  private UsuarioRolRepository usuarioRolRepository;

  @Mock
  private RolPermisoRepository rolPermisoRepository;

  @InjectMocks
  private AuthService authService;

  private Usuario usuario;
  private Rol rolAdmin;
  private Permiso permisoCrearPedido;
  private UserDetails userDetails;

  private static final LocalDateTime TEST_NOW = LocalDateTime.of(2026, 6, 13, 10, 0);

  @BeforeEach
  void setUp() {
    usuario = Usuario.builder()
        .username("testUser")
        .email("test@kantus.com")
        .passwordHash("hashedPass")
        .intentosFallidos((short) 0)
        .mfaHabilitado(false)
        .requiereCambioPassword(false)
        .build();
    usuario.setId(UUID.randomUUID());

    rolAdmin = Rol.builder()
        .nombre("ROLE_ADMIN")
        .descripcion("Administrador")
        .esSistema(true)
        .build();
    rolAdmin.setId(UUID.randomUUID());

    permisoCrearPedido = Permiso.builder()
        .codigo("order:create")
        .modulo("ORDER")
        .descripcion("Crear pedidos")
        .build();
    permisoCrearPedido.setId(UUID.randomUUID());

    userDetails = User.builder()
        .username(usuario.getId().toString())
        .password(usuario.getPasswordHash())
        .authorities("ROLE_ADMIN")
        .build();
  }

  @Test
  @DisplayName("Debe lanzar CredencialesInvalidasException cuando el usuario no existe")
  void shouldThrowExceptionWhenUserNotFound() {
    when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

    LoginRequest request = new LoginRequest("unknown", "pass");

    assertThrows(CredencialesInvalidasException.class, () ->
        authService.autenticarUsuario(request, "127.0.0.1", "test-agent")
    );

    verify(authenticationManager, never()).authenticate(any());
  }

  @Test
  @DisplayName("Debe autenticar correctamente y emitir JWT con roles y permisos")
  @SuppressWarnings("unchecked")
  void shouldReturnAuthResponseWithRolesAndPermissionsWhenLoginIsSuccessful() {
    when(usuarioRepository.findByUsername("testUser")).thenReturn(Optional.of(usuario));
    when(userDetailsService.loadUserByUsername(usuario.getId().toString()))
        .thenReturn(userDetails);
    when(usuarioRolRepository.findByUsuarioId(usuario.getId()))
        .thenReturn(List.of(UsuarioRol.builder().usuario(usuario).rol(rolAdmin).build()));
    when(rolPermisoRepository.findByRolIdIn(anyCollection()))
        .thenReturn(List.of(RolPermiso.builder()
            .rol(rolAdmin)
            .permiso(permisoCrearPedido)
            .build()));
    when(jwtService.generateToken(anyMap(), eq(userDetails))).thenReturn("jwt-token");

    LoginRequest request = new LoginRequest("testUser", "password");

    AuthResponse response = authService.autenticarUsuario(request, "127.0.0.1", "agent");

    assertNotNull(response);
    assertEquals("jwt-token", response.getToken());
    assertNotNull(response.getRefreshToken());

    ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);

    verify(jwtService).generateToken(claimsCaptor.capture(), eq(userDetails));

    Map<String, Object> claims = claimsCaptor.getValue();

    assertEquals("testUser", claims.get("username"));
    assertTrue(((List<String>) claims.get("roles")).contains("ADMIN"));
    assertTrue(((List<String>) claims.get("permissions")).contains("order:create"));

    verify(usuarioRepository, times(1)).save(usuario);
    verify(sesionRepository, times(1)).save(any(Sesion.class));
    verify(auditLogRepository, times(1)).save(any(AuthAuditLog.class));
  }

  @Test
  @DisplayName("Debe lanzar UsuarioBloqueadoException cuando el usuario tiene bloqueo activo")
  void shouldThrowUsuarioBloqueadoExceptionWhenAccountIsLocked() {
    usuario.setBloqueadoHasta(TEST_NOW.plusMinutes(10));
    when(usuarioRepository.findByUsername("testUser")).thenReturn(Optional.of(usuario));

    LoginRequest request = new LoginRequest("testUser", "pass");

    assertThrows(UsuarioBloqueadoException.class, () ->
        authService.autenticarUsuario(request, "127.0.0.1", "agent")
    );

    verify(authenticationManager, never()).authenticate(any());
    verify(auditLogRepository, times(1)).save(any(AuthAuditLog.class));
  }

  @Test
  @DisplayName("Debe incrementar intentos fallidos cuando la contraseña es incorrecta")
  void shouldHandleFailedAttemptAndIncrementCount() {
    when(usuarioRepository.findByUsername("testUser")).thenReturn(Optional.of(usuario));
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    LoginRequest request = new LoginRequest("testUser", "wrong");

    assertThrows(CredencialesInvalidasException.class, () ->
        authService.autenticarUsuario(request, "127.0.0.1", "agent")
    );

    assertEquals((short) 1, usuario.getIntentosFallidos());
    assertEquals(null, usuario.getBloqueadoHasta());

    verify(usuarioRepository, times(1)).save(usuario);
    verify(auditLogRepository, times(1)).save(any(AuthAuditLog.class));
  }

  @Test
  @DisplayName("Debe bloquear usuario cuando llega al tercer intento fallido")
  void shouldBlockUserAfterThreeFailedAttempts() {
    usuario.setIntentosFallidos((short) 2);

    when(usuarioRepository.findByUsername("testUser")).thenReturn(Optional.of(usuario));
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    LoginRequest request = new LoginRequest("testUser", "wrong");

    assertThrows(CredencialesInvalidasException.class, () ->
        authService.autenticarUsuario(request, "127.0.0.1", "agent")
    );

    assertEquals((short) 3, usuario.getIntentosFallidos());
    assertNotNull(usuario.getBloqueadoHasta());
    assertTrue(usuario.getBloqueadoHasta().isAfter(TEST_NOW));

    verify(usuarioRepository, times(1)).save(usuario);
    verify(auditLogRepository, times(1)).save(any(AuthAuditLog.class));
  }

  @Test
  @DisplayName("Debe refrescar token correctamente cuando el refresh token es válido")
  void shouldRefreshTokenWhenRefreshTokenIsValid() {
    Sesion sesion = Sesion.builder()
        .usuario(usuario)
        .refreshTokenHash("refresh-token")
        .fechaExpiracion(TEST_NOW.plusDays(1))
        .revocado(false)
        .build();

    when(sesionRepository.findByRefreshTokenHash("refresh-token"))
        .thenReturn(Optional.of(sesion));
    when(userDetailsService.loadUserByUsername(usuario.getId().toString()))
        .thenReturn(userDetails);
    when(usuarioRolRepository.findByUsuarioId(usuario.getId()))
        .thenReturn(List.of(UsuarioRol.builder().usuario(usuario).rol(rolAdmin).build()));
    when(rolPermisoRepository.findByRolIdIn(anyCollection()))
        .thenReturn(List.of(RolPermiso.builder()
            .rol(rolAdmin)
            .permiso(permisoCrearPedido)
            .build()));
    when(jwtService.generateToken(anyMap(), eq(userDetails))).thenReturn("new-jwt-token");

    AuthResponse response = authService.refrescarToken("refresh-token", "127.0.0.1");

    assertNotNull(response);
    assertEquals("new-jwt-token", response.getToken());
    assertEquals("refresh-token", response.getRefreshToken());
  }

  @Test
  @DisplayName("Debe lanzar excepción cuando el refresh token no existe")
  void shouldThrowExceptionWhenRefreshTokenDoesNotExist() {
    when(sesionRepository.findByRefreshTokenHash("invalid-token"))
        .thenReturn(Optional.empty());

    assertThrows(CredencialesInvalidasException.class, () ->
        authService.refrescarToken("invalid-token", "127.0.0.1")
    );
  }

  @Test
  @DisplayName("Debe lanzar excepción cuando el refresh token fue revocado")
  void shouldThrowExceptionWhenRefreshTokenIsRevoked() {
    Sesion sesion = Sesion.builder()
        .usuario(usuario)
        .refreshTokenHash("refresh-token")
        .fechaExpiracion(TEST_NOW.plusDays(1))
        .revocado(true)
        .build();

    when(sesionRepository.findByRefreshTokenHash("refresh-token"))
        .thenReturn(Optional.of(sesion));

    assertThrows(CredencialesInvalidasException.class, () ->
        authService.refrescarToken("refresh-token", "127.0.0.1")
    );
  }

  @Test
  @DisplayName("Debe lanzar excepción cuando el refresh token expiró")
  void shouldThrowExceptionWhenRefreshTokenIsExpired() {
    Sesion sesion = Sesion.builder()
        .usuario(usuario)
        .refreshTokenHash("refresh-token")
        .fechaExpiracion(TEST_NOW.minusMinutes(1))
        .revocado(false)
        .build();

    when(sesionRepository.findByRefreshTokenHash("refresh-token"))
        .thenReturn(Optional.of(sesion));

    assertThrows(CredencialesInvalidasException.class, () ->
        authService.refrescarToken("refresh-token", "127.0.0.1")
    );
  }

  @Test
  @DisplayName("Debe emitir JWT con listas vacías cuando el usuario no tiene roles")
  @SuppressWarnings("unchecked")
  void shouldGenerateJwtWithEmptyRolesAndPermissionsWhenUserHasNoRoles() {
    when(usuarioRepository.findByUsername("testUser")).thenReturn(Optional.of(usuario));
    when(userDetailsService.loadUserByUsername(usuario.getId().toString()))
        .thenReturn(userDetails);
    when(usuarioRolRepository.findByUsuarioId(usuario.getId()))
        .thenReturn(List.of());
    when(jwtService.generateToken(anyMap(), eq(userDetails))).thenReturn("jwt-token");

    LoginRequest request = new LoginRequest("testUser", "password");

    AuthResponse response = authService.autenticarUsuario(request, "127.0.0.1", "agent");

    assertNotNull(response);

    ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);

    verify(jwtService).generateToken(claimsCaptor.capture(), eq(userDetails));

    Map<String, Object> claims = claimsCaptor.getValue();

    assertTrue(((List<String>) claims.get("roles")).isEmpty());
    assertTrue(((List<String>) claims.get("permissions")).isEmpty());

    verify(rolPermisoRepository, never()).findByRolIdIn(anyCollection());
  }
}