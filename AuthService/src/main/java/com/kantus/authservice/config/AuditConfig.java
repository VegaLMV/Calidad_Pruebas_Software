package com.kantus.authservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración global para habilitar la auditoría de Spring Data JPA.
 * Enlaza las anotaciones de las entidades con el componente "auditorProvider".
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {
  // La anotación de clase hace todo el trabajo.
  // No requiere lógica adicional por ahora.
}