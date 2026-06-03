package com.kantus.authservice.util;

/**
 * Centraliza las constantes utilizadas en la configuración de seguridad y JWT.
 */
public final class SecurityConstants {

  private SecurityConstants() {
    throw new UnsupportedOperationException(
        "Esta es una clase de utilidades y no puede ser instanciada");
  }

  public static final String AUTH_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  // Rutas públicas que no requieren autenticación
  public static final String[] PUBLIC_MATCHERS = {
      "/api/v1/auth/login",
      "/api/v1/auth/refresh-token",
      "/api/v1/auth/forgot-password",
      "/api/v1/usuarios/registro",
      "/v3/api-docs/**",
      "/swagger-ui/**",
      "/swagger-ui.html"
  };
}