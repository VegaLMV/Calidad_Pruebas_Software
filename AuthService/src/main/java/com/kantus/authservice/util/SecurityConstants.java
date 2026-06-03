package com.kantus.authservice.util;

/**
 * Clase utilitaria que contiene las constantes de configuración de seguridad.
 * Almacena los prefijos de tokens y las rutas públicas del sistema.
 */
public class SecurityConstants {

  public static final String AUTH_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  // 1. Lo hacemos private para que nadie pueda modificar el original
  private static final String[] PUBLIC_MATCHERS = {
      "/api/v1/auth/login",
      "/api/v1/auth/refresh-token",
      "/api/v1/auth/forgot-password",
      "/api/v1/usuarios/registro",
      "/v3/api-docs/**",
      "/swagger-ui/**",
      "/swagger-ui.html"
  };

  private SecurityConstants() {
    // Constructor vacío para que Checkstyle y Sonar no pidan instanciar esta clase
  }

  // 2. Creamos un método que entrega una "copia" segura del array
  public static String[] getPublicMatchers() {
    return PUBLIC_MATCHERS.clone();
  }
}