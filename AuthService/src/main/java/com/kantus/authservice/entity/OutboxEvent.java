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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Evento transaccional pendiente de publicación hacia otros microservicios.
 *
 * <p>Implementa el patrón Outbox: primero se guarda el evento en la misma base
 * de datos del microservicio y luego un publicador lo envía a RabbitMQ.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

  public static final String STATUS_PENDING = "PENDING";
  public static final String STATUS_SENT = "SENT";
  public static final String STATUS_FAILED = "FAILED";

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "aggregate_type", nullable = false, length = 80)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "event_type", nullable = false, length = 120)
  private String eventType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
  private String payload;

  @Builder.Default
  @Column(name = "status", nullable = false, length = 20)
  private String status = STATUS_PENDING;

  @Builder.Default
  @Column(name = "retry_count", nullable = false)
  private Integer retryCount = 0;

  @Column(name = "error_message")
  private String errorMessage;

  @Builder.Default
  @Column(name = "fecha_creacion", nullable = false)
  private LocalDateTime fechaCreacion = DateTimeProvider.now();

  @Column(name = "fecha_publicacion")
  private LocalDateTime fechaPublicacion;

  /**
   * Inicializa valores por defecto antes de persistir el evento Outbox.
   */
  @PrePersist
  protected void prePersist() {
    if (status == null) {
      status = STATUS_PENDING;
    }

    if (retryCount == null) {
      retryCount = 0;
    }

    if (fechaCreacion == null) {
      fechaCreacion = DateTimeProvider.now();
    }
  }
}