package com.kantus.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de inicio de la aplicación de autenticación.
 */
@SpringBootApplication
public class AuthServiceApplication {

  /**
   * Método principal para arrancar el microservicio.
   *
   * @param args argumentos de línea de comandos.
   */
  public static void main(String[] args) {
    SpringApplication.run(AuthServiceApplication.class, args);
  }
}