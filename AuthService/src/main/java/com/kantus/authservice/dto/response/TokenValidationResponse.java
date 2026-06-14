package com.kantus.authservice.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta para validación del token JWT actual.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponse {

  private boolean valid;
  private UUID userId;
  private String username;
  private List<String> roles;
  private List<String> permissions;
}