package com.kantus.authservice.exception;

public class UsuarioBloqueadoException extends RuntimeException {
  public UsuarioBloqueadoException(String mensaje) {
    super(mensaje);
  }
}