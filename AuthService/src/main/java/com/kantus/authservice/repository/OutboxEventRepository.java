package com.kantus.authservice.repository;

import com.kantus.authservice.entity.OutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar eventos Outbox pendientes de publicación.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

  /**
   * Obtiene los primeros 50 eventos pendientes ordenados por fecha de creación.
   *
   * @param status Estado del evento.
   * @return Lista de eventos pendientes.
   */
  List<OutboxEvent> findTop50ByStatusOrderByFechaCreacionAsc(String status);
}