package com.kantus.authservice.exception;

/**
 * Excepción lanzada cuando las credenciales proporcionadas no son válidas.
 */
public class CredencialesInvalidasException extends RuntimeException {

  /**
   * Crea una nueva instancia de CredencialesInvalidasException.
   *
   * @param mensaje Descripción del error.
   */
  public CredencialesInvalidasException(String mensaje) {
    super(mensaje);
  }
}