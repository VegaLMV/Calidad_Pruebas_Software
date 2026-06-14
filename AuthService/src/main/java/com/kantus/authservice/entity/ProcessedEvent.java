package com.kantus.authservice.entity;

import com.kantus.authservice.util.DateTimeProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registra eventos externos ya procesados para evitar duplicidad.
 *
 * <p>Esta tabla permite idempotencia cuando RabbitMQ reenvía un mensaje
 * o cuando un consumidor procesa el mismo evento más de una vez.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "event_id", nullable = false, unique = true)
  private UUID eventId;

  @Column(name = "event_type", nullable = false, length = 120)
  private String eventType;

  @Column(name = "source_service", nullable = false, length = 80)
  private String sourceService;

  @Builder.Default
  @Column(name = "processed_at", nullable = false)
  private LocalDateTime processedAt = DateTimeProvider.now();

  /**
   * Inicializa la fecha de procesamiento antes de persistir el evento procesado.
   */
  @PrePersist
  protected void prePersist() {
    if (processedAt == null) {
      processedAt = DateTimeProvider.now();
    }
  }
}