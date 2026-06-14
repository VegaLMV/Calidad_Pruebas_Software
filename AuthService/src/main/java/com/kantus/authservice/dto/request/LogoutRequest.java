package com.kantus.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request para cerrar sesión revocando un refresh token.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

  @NotBlank(message = "El refresh token es obligatorio")
  private String refreshToken;
}