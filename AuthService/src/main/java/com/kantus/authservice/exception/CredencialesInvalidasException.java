package com.kantus.authservice.exception;

public class CredencialesInvalidasException extends RuntimeException {
  public CredencialesInvalidasException(String mensaje) {
    super(mensaje);
  }
}