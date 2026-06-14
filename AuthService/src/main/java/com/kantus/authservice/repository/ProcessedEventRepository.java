package com.kantus.authservice.repository;

import com.kantus.authservice.entity.ProcessedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para controlar eventos ya procesados.
 */
@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

  /**
   * Verifica si un evento externo ya fue procesado.
   *
   * @param eventId Identificador único del evento.
   * @return true si el evento ya fue procesado.
   */
  boolean existsByEventId(UUID eventId);
}