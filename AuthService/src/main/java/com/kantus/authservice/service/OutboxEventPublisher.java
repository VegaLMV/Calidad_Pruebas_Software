package com.kantus.authservice.service;

import com.kantus.authservice.config.RabbitMqConfig;
import com.kantus.authservice.entity.OutboxEvent;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Publicador de eventos Outbox hacia RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventPublisher {

  private final RabbitTemplate rabbitTemplate;
  private final OutboxEventService outboxEventService;

  @Value("${kantus.events.source-service:AuthService}")
  private String sourceService;

  /**
   * Publica periódicamente los eventos pendientes registrados en outbox_events.
   */
  @Scheduled(fixedDelayString = "${kantus.outbox.publisher.fixed-delay-ms:5000}")
  public void publicarEventosPendientes() {
    List<OutboxEvent> eventosPendientes = outboxEventService.obtenerEventosPendientes();

    for (OutboxEvent event : eventosPendientes) {
      publicarEvento(event);
    }
  }

  /**
   * Publica un evento individual en RabbitMQ.
   *
   * @param event Evento Outbox.
   */
  public void publicarEvento(OutboxEvent event) {
    String routingKey = resolverRoutingKey(event.getEventType());

    try {
      Message message = construirMensaje(event);

      rabbitTemplate.send(
          RabbitMqConfig.KANTUS_EVENTS_EXCHANGE,
          routingKey,
          message
      );

      outboxEventService.marcarComoPublicado(event.getId());

      log.info(
          "Evento publicado correctamente. eventId={}, eventType={}, routingKey={}",
          event.getId(),
          event.getEventType(),
          routingKey
      );
    } catch (AmqpException ex) {
      outboxEventService.marcarComoFallido(event.getId(), ex.getMessage());

      log.error(
          "Error al publicar evento. eventId={}, eventType={}, error={}",
          event.getId(),
          event.getEventType(),
          ex.getMessage()
      );
    }
  }

  private Message construirMensaje(OutboxEvent event) {
    MessageProperties properties = new MessageProperties();
    properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
    properties.setHeader("eventId", event.getId().toString());
    properties.setHeader("eventType", event.getEventType());
    properties.setHeader("aggregateType", event.getAggregateType());
    properties.setHeader("aggregateId", event.getAggregateId().toString());
    properties.setHeader("sourceService", sourceService);

    byte[] body = event.getPayload().getBytes(StandardCharsets.UTF_8);

    return new Message(body, properties);
  }

  private String resolverRoutingKey(String eventType) {
    if ("UserRegistered".equals(eventType)) {
      return "auth.user.registered";
    }

    if ("UserRoleAssigned".equals(eventType)) {
      return "auth.user.role-assigned";
    }

    if ("PermissionsChanged".equals(eventType)) {
      return "auth.permissions.changed";
    }

    return "auth." + eventType
        .replaceAll("([a-z])([A-Z])", "$1.$2")
        .replace("_", ".")
        .toLowerCase(Locale.ROOT);
  }
}