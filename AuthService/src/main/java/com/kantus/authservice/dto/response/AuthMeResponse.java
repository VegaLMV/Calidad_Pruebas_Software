package com.kantus.authservice.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta con la información del usuario autenticado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthMeResponse {

  private UUID id;
  private String username;
  private String email;
  private List<String> roles;
  private List<String> permissions;
  private boolean requiereCambioPassword;
  private boolean mfaHabilitado;
}