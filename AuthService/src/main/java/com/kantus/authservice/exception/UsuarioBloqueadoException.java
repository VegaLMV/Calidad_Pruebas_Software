package com.kantus.authservice.exception;

/**
 * Excepción personalizada que se lanza cuando un usuario intenta acceder con una cuenta bloqueada.
 */
public class UsuarioBloqueadoException extends RuntimeException {

  /**
   * Crea una nueva instancia de la excepción con un mensaje específico.
   *
   * @param mensaje El mensaje descriptivo del error.
   */
  public UsuarioBloqueadoException(String mensaje) {
    super(mensaje);
  }
}