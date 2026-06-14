package com.kantus.authservice.exception;

/**
 * Excepción lanzada cuando no existe un recurso solicitado.
 */
public class RecursoNoEncontradoException extends RuntimeException {

  public RecursoNoEncontradoException(String mensaje) {
    super(mensaje);
  }

}