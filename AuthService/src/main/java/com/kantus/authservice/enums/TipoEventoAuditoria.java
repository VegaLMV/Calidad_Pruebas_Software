package com.kantus.authservice.enums;

/**
 * Define los tipos de eventos críticos de seguridad que serán registrados
 * en la bitácora de auditoría (auth_audit_log).
 */
public enum TipoEventoAuditoria {
  LOGIN_EXITOSO,
  LOGIN_FALLIDO,
  CAMBIO_PASSWORD,
  DESBLOQUEO_CUENTA,
  TOKEN_REVOCADO
}
