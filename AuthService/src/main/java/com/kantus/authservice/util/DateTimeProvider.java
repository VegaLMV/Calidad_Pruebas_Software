package com.kantus.authservice.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Proveedor centralizado de fecha y hora del sistema.
 */
public final class DateTimeProvider {

  private static final ZoneId APP_ZONE = ZoneId.of("America/Lima");

  private DateTimeProvider() {
  }

  /**
   * Devuelve la fecha y hora actual usando la zona horaria de la aplicación.
   *
   * @return Fecha y hora actual.
   */
  public static LocalDateTime now() {
    return LocalDateTime.now(APP_ZONE);
  }
}