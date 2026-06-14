package com.kantus.authservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kantus.authservice.config.RabbitMqConfig;
import com.kantus.authservice.entity.OutboxEvent;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Pruebas unitarias del publicador Outbox hacia RabbitMQ.
 */
class OutboxEventPublisherTest {

  private RabbitTemplate rabbitTemplate;
  private OutboxEventService outboxEventService;
  private OutboxEventPublisher publisher;

  @BeforeEach
  void setUp() {
    rabbitTemplate = org.mockito.Mockito.mock(RabbitTemplate.class);
    outboxEventService = org.mockito.Mockito.mock(OutboxEventService.class);
    publisher = new OutboxEventPublisher(rabbitTemplate, outboxEventService);

    ReflectionTestUtils.setField(publisher, "sourceService", "AuthService");
  }

  @Test
  @DisplayName("Debe publicar evento UserRegistered y marcarlo como publicado")
  void shouldPublishUserRegisteredEventAndMarkAsSent() {
    OutboxEvent event = crearEvento("UserRegistered");

    publisher.publicarEvento(event);

    verify(rabbitTemplate, times(1)).send(
        eq(RabbitMqConfig.KANTUS_EVENTS_EXCHANGE),
        eq("auth.user.registered"),
        any(Message.class)
    );

    verify(outboxEventService, times(1)).marcarComoPublicado(event.getId());
  }

  @Test
  @DisplayName("Debe publicar evento UserRoleAssigned con routing key correcta")
  void shouldPublishUserRoleAssignedWithCorrectRoutingKey() {
    OutboxEvent event = crearEvento("UserRoleAssigned");

    publisher.publicarEvento(event);

    verify(rabbitTemplate, times(1)).send(
        eq(RabbitMqConfig.KANTUS_EVENTS_EXCHANGE),
        eq("auth.user.role-assigned"),
        any(Message.class)
    );

    verify(outboxEventService, times(1)).marcarComoPublicado(event.getId());
  }

  @Test
  @DisplayName("Debe marcar evento como fallido cuando RabbitMQ lanza error")
  void shouldMarkEventAsFailedWhenRabbitMqFails() {
    OutboxEvent event = crearEvento("UserRegistered");

    doThrow(new AmqpException("RabbitMQ no disponible"))
        .when(rabbitTemplate)
        .send(
            eq(RabbitMqConfig.KANTUS_EVENTS_EXCHANGE),
            eq("auth.user.registered"),
            any(Message.class)
        );

    publisher.publicarEvento(event);

    verify(outboxEventService, times(1))
        .marcarComoFallido(event.getId(), "RabbitMQ no disponible");
  }

  @Test
  @DisplayName("Debe publicar todos los eventos pendientes")
  void shouldPublishAllPendingEvents() {
    OutboxEvent eventOne = crearEvento("UserRegistered");
    OutboxEvent eventTwo = crearEvento("PermissionsChanged");

    when(outboxEventService.obtenerEventosPendientes())
        .thenReturn(List.of(eventOne, eventTwo));

    publisher.publicarEventosPendientes();

    verify(rabbitTemplate, times(1)).send(
        eq(RabbitMqConfig.KANTUS_EVENTS_EXCHANGE),
        eq("auth.user.registered"),
        any(Message.class)
    );

    verify(rabbitTemplate, times(1)).send(
        eq(RabbitMqConfig.KANTUS_EVENTS_EXCHANGE),
        eq("auth.permissions.changed"),
        any(Message.class)
    );

    verify(outboxEventService, times(1)).marcarComoPublicado(eventOne.getId());
    verify(outboxEventService, times(1)).marcarComoPublicado(eventTwo.getId());
  }

  private OutboxEvent crearEvento(String eventType) {
    UUID eventId = UUID.randomUUID();

    return OutboxEvent.builder()
        .id(eventId)
        .aggregateType("Usuario")
        .aggregateId(UUID.randomUUID())
        .eventType(eventType)
        .payload("{\"eventId\":\"" + eventId + "\"}")
        .status(OutboxEvent.STATUS_PENDING)
        .retryCount(0)
        .build();
  }
}