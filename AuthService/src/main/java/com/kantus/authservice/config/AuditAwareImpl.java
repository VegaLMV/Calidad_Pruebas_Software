package com.kantus.authservice.config;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Componente encargado de proveer el identificador (UUID) del usuario actual
 * para la auditoría automática de JPA (@CreatedBy, @LastModifiedBy).
 */
@Component("auditorProvider")
public class AuditAwareImpl implements AuditorAware<UUID> {

  // UUID reservado que representa al "SISTEMA" (Ideal para auto-registros públicos)
  private static final UUID SYSTEM_UUID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");

  @Override
  public Optional<UUID> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 1. Si no hay sesión, devolvemos el UUID del Sistema en lugar de vacío
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication.getPrincipal().equals("anonymousUser")) {
      return Optional.of(SYSTEM_UUID);
    }

    try {
      // 2. Extraer el UUID del Token
      String currentUserId = authentication.getName();
      return Optional.of(UUID.fromString(currentUserId));

    } catch (IllegalArgumentException e) {
      // Protección contra tokens malformados
      return Optional.of(SYSTEM_UUID);
    }
  }
}