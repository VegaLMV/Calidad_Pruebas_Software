package com.kantus.authservice.service;

import com.kantus.authservice.dto.request.LoginRequest;
import com.kantus.authservice.dto.response.AuthResponse;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.exception.CredencialesInvalidasException;
import com.kantus.authservice.exception.UsuarioBloqueadoException;
import com.kantus.authservice.repository.AuthAuditLogRepository;
import com.kantus.authservice.repository.SesionRepository;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.security.CustomUserDetailsService;
import com.kantus.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para {@link AuthService}.
 * Utiliza Mockito para aislar la lógica de negocio de las dependencias externas
 * (BD, Seguridad y Auditoría), cumpliendo con los estándares de calidad de software
 * para pruebas de integración aisladas.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UsuarioRepository usuarioRepository;
  @Mock private JwtService jwtService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private AuthAuditLogRepository auditLogRepository;
  @Mock private CustomUserDetailsService userDetailsService;
  @Mock private SesionRepository sesionRepository;

  @InjectMocks private AuthService authService;

  private Usuario mockUsuario;

  @BeforeEach
  void setUp() {
    mockUsuario = Usuario.builder()
        .username("testUser")
        .passwordHash("hashedPass")
        .intentosFallidos((short) 0)
        .build();
    mockUsuario.setId(UUID.randomUUID());
  }

  @Test
  @DisplayName("Debe lanzar excepción cuando el usuario no existe en la base de datos")
  void shouldThrowExceptionWhenUserNotFound() {
    // Arrange: Escenario de usuario no registrado
    when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

    LoginRequest request = new LoginRequest("unknown", "pass");

    // Act & Assert: Validar comportamiento ante usuario inexistente
    assertThrows(RuntimeException.class, () ->
        authService.autenticarUsuario(request, "127.0.0.1", "test-agent")
    );
  }

  @Test
  @DisplayName("Debe retornar AuthResponse exitoso cuando las credenciales son correctas")
  void shouldReturnAuthResponseWhenLoginIsSuccessful() {
    // Arrange: Escenario de login válido
    when(usuarioRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUsuario));
    // Se mockea el comportamiento exitoso de autenticación y generación de token

    // Act: Ejecutar el proceso de autenticación
    AuthResponse response = authService.autenticarUsuario(
        new LoginRequest("testUser", "password"), "127.0.0.1", "agent");

    // Assert: Validar respuesta y persistencia de auditoría
    assertNotNull(response);
    verify(usuarioRepository, times(1)).save(any(Usuario.class));
  }

  @Test
  @DisplayName("Debe lanzar UsuarioBloqueadoException cuando el usuario tiene bloqueo activo")
  void shouldThrowUsuarioBloqueadoExceptionWhenAccountIsLocked() {
    // Arrange: Simular usuario bloqueado con fecha de desbloqueo a futuro
    mockUsuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(10));
    when(usuarioRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUsuario));

    // CORRECCIÓN: Creamos la petición afuera
    LoginRequest request = new LoginRequest("testUser", "pass");

    // Act & Assert: Validar que el sistema restringe el acceso
    assertThrows(UsuarioBloqueadoException.class, () ->
        authService.autenticarUsuario(request, "127.0.0.1", "agent")
    );
  }

  @Test
  @DisplayName("Debe incrementar contador de fallidos ante contraseña incorrecta")
  void shouldHandleFailedAttemptAndIncrementCount() {
    // Arrange: Simular fallo de autenticación de Spring Security
    when(usuarioRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUsuario));
    when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad"));

    // CORRECCIÓN: Creamos la petición afuera
    LoginRequest request = new LoginRequest("testUser", "wrong");

    // Act & Assert: Validar excepción y efecto secundario (incremento de intentos)
    assertThrows(CredencialesInvalidasException.class, () ->
        authService.autenticarUsuario(request, "127.0.0.1", "agent")
    );
    assertEquals((short) 1, mockUsuario.getIntentosFallidos());
  }
}