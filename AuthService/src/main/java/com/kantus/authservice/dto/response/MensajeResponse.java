package com.kantus.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de transferencia de datos para respuestas genéricas del sistema.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MensajeResponse {

  private String mensaje;
  private int codigo;
}