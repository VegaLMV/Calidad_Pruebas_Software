package com.kantus.authservice.service;

import com.kantus.authservice.entity.Rol;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.util.DateTimeProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de registrar eventos de dominio del AuthService.
 */
@Service
@RequiredArgsConstructor
public class AuthDomainEventService {

  private static final String AGGREGATE_USUARIO = "Usuario";
  private static final String AGGREGATE_ROL = "Rol";
  private static final String PAYLOAD_FECHA_EVENTO = "fechaEvento";

  private final OutboxEventService outboxEventService;

  /**
   * Registra evento de usuario creado.
   *
   * @param usuario Usuario creado.
   */
  public void registrarUserRegistered(Usuario usuario) {
    outboxEventService.registrarEvento(
        AGGREGATE_USUARIO,
        usuario.getId(),
        "UserRegistered",
        Map.of(
            "usuarioId", usuario.getId().toString(),
            "username", usuario.getUsername(),
            "email", usuario.getEmail(),
            PAYLOAD_FECHA_EVENTO, DateTimeProvider.now().toString()
        )
    );
  }

  /**
   * Registra evento de rol asignado a usuario.
   *
   * @param usuario Usuario.
   * @param rol Rol asignado.
   */
  public void registrarUserRoleAssigned(Usuario usuario, Rol rol) {
    outboxEventService.registrarEvento(
        AGGREGATE_USUARIO,
        usuario.getId(),
        "UserRoleAssigned",
        Map.of(
            "usuarioId", usuario.getId().toString(),
            "username", usuario.getUsername(),
            "rolId", rol.getId().toString(),
            "rolNombre", rol.getNombre(),
            PAYLOAD_FECHA_EVENTO, DateTimeProvider.now().toString()
        )
    );
  }

  /**
   * Registra evento de cambio de permisos.
   *
   * <p>Este método se usará cuando se cree el servicio de administración de roles
   * y permisos.</p>
   *
   * @param rol Rol afectado.
   * @param motivo Motivo o descripción del cambio.
   */
  public void registrarPermissionsChanged(Rol rol, String motivo) {
    outboxEventService.registrarEvento(
        AGGREGATE_ROL,
        rol.getId(),
        "PermissionsChanged",
        Map.of(
            "rolId", rol.getId().toString(),
            "rolNombre", rol.getNombre(),
            "motivo", motivo,
            PAYLOAD_FECHA_EVENTO, DateTimeProvider.now().toString()
        )
    );
  }
}