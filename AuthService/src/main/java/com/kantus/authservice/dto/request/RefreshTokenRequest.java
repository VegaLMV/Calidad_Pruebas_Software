package com.kantus.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Objeto de transferencia de datos para la solicitud de refresco de token.
 */
@Data
public class RefreshTokenRequest {

  @NotBlank(message = "El Refresh Token es obligatorio")
  private String refreshToken;
}