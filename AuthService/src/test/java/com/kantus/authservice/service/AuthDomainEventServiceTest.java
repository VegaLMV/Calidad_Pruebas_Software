package com.kantus.authservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.kantus.authservice.entity.Rol;
import com.kantus.authservice.entity.Usuario;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Pruebas unitarias de los eventos de dominio del AuthService.
 */
class AuthDomainEventServiceTest {

  private OutboxEventService outboxEventService;
  private AuthDomainEventService authDomainEventService;

  @BeforeEach
  void setUp() {
    outboxEventService = org.mockito.Mockito.mock(OutboxEventService.class);
    authDomainEventService = new AuthDomainEventService(outboxEventService);
  }

  @Test
  @DisplayName("Debe registrar evento UserRegistered")
  void shouldRegisterUserRegisteredEvent() {
    Usuario usuario = crearUsuario();

    authDomainEventService.registrarUserRegistered(usuario);

    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

    verify(outboxEventService, times(1)).registrarEvento(
        eq("Usuario"),
        eq(usuario.getId()),
        eq("UserRegistered"),
        payloadCaptor.capture()
    );

    Map<String, Object> payload = payloadCaptor.getValue();

    assertEquals(usuario.getId().toString(), payload.get("usuarioId"));
    assertEquals(usuario.getUsername(), payload.get("username"));
    assertEquals(usuario.getEmail(), payload.get("email"));
  }

  @Test
  @DisplayName("Debe registrar evento UserRoleAssigned")
  void shouldRegisterUserRoleAssignedEvent() {
    Usuario usuario = crearUsuario();
    Rol rol = crearRol();

    authDomainEventService.registrarUserRoleAssigned(usuario, rol);

    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

    verify(outboxEventService, times(1)).registrarEvento(
        eq("Usuario"),
        eq(usuario.getId()),
        eq("UserRoleAssigned"),
        payloadCaptor.capture()
    );

    Map<String, Object> payload = payloadCaptor.getValue();

    assertEquals(usuario.getId().toString(), payload.get("usuarioId"));
    assertEquals(rol.getId().toString(), payload.get("rolId"));
    assertEquals("ROLE_ADMIN", payload.get("rolNombre"));
  }

  @Test
  @DisplayName("Debe registrar evento PermissionsChanged")
  void shouldRegisterPermissionsChangedEvent() {
    Rol rol = crearRol();

    authDomainEventService.registrarPermissionsChanged(rol, "Permiso asignado");

    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

    verify(outboxEventService, times(1)).registrarEvento(
        eq("Rol"),
        eq(rol.getId()),
        eq("PermissionsChanged"),
        payloadCaptor.capture()
    );

    Map<String, Object> payload = payloadCaptor.getValue();

    assertEquals(rol.getId().toString(), payload.get("rolId"));
    assertEquals("ROLE_ADMIN", payload.get("rolNombre"));
    assertEquals("Permiso asignado", payload.get("motivo"));
  }

  private Usuario crearUsuario() {
    Usuario usuario = Usuario.builder()
        .username("superadmin")
        .email("admin@kantus.com")
        .passwordHash("hash")
        .build();
    usuario.setId(UUID.randomUUID());

    return usuario;
  }

  private Rol crearRol() {
    Rol rol = Rol.builder()
        .nombre("ROLE_ADMIN")
        .descripcion("Administrador")
        .esSistema(true)
        .build();
    rol.setId(UUID.randomUUID());

    return rol;
  }
}