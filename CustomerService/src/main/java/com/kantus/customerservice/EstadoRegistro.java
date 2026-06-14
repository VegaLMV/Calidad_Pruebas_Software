package com.kantus.customerservice;

/**
 * Define los estados lógicos de un registro en la base de datos.
 * Se utiliza para aplicar el patrón de borrado lógico (Soft Delete).
 */
public enum EstadoRegistro {
  ACTIVO,
  INACTIVO
}
