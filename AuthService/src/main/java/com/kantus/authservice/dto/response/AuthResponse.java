package com.kantus.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que contiene la respuesta de autenticación con los tokens y mensaje.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
  private String token;
  private String refreshToken;
  private String mensaje;
}