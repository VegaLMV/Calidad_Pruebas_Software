package com.kantus.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de Jackson para serialización JSON.
 */
@Configuration
public class JacksonConfig {

  /**
   * Bean principal de ObjectMapper usado por los servicios del sistema.
   *
   * @return ObjectMapper configurado.
   */
  @Bean
  public ObjectMapper objectMapper() {
    return JsonMapper.builder()
        .findAndAddModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();
  }
}