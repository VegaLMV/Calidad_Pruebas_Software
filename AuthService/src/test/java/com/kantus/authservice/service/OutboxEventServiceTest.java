package com.kantus.authservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kantus.authservice.entity.OutboxEvent;
import com.kantus.authservice.exception.CredencialesInvalidasException;
import com.kantus.authservice.repository.OutboxEventRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias de OutboxEventService.
 */
@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

  @Mock
  private OutboxEventRepository outboxEventRepository;

  private ObjectMapper objectMapper;

  @InjectMocks
  private OutboxEventService outboxEventService;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    outboxEventService = new OutboxEventService(outboxEventRepository, objectMapper);
  }

  @Test
  @DisplayName("Debe registrar evento outbox con payload JSON")
  void shouldRegisterOutboxEventWithJsonPayload() {
    UUID aggregateId = UUID.randomUUID();

    when(outboxEventRepository.save(any(OutboxEvent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    OutboxEvent event = outboxEventService.registrarEvento(
        "Usuario",
        aggregateId,
        "UserRegistered",
        "{\"usuarioId\":\"" + aggregateId + "\"}"
    );

    assertNotNull(event);
    assertEquals("Usuario", event.getAggregateType());
    assertEquals(aggregateId, event.getAggregateId());
    assertEquals("UserRegistered", event.getEventType());
    assertEquals(OutboxEvent.STATUS_PENDING, event.getStatus());
    assertEquals(0, event.getRetryCount());

    verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));
  }

  @Test
  @DisplayName("Debe registrar evento outbox convirtiendo Map a JSON")
  void shouldRegisterOutboxEventWithMapPayload() {
    UUID aggregateId = UUID.randomUUID();

    when(outboxEventRepository.save(any(OutboxEvent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    OutboxEvent event = outboxEventService.registrarEvento(
        "Usuario",
        aggregateId,
        "UserRegistered",
        Map.of(
            "usuarioId", aggregateId.toString(),
            "username", "superadmin"
        )
    );

    assertNotNull(event);
    assertEquals("Usuario", event.getAggregateType());
    assertEquals("UserRegistered", event.getEventType());
    assertEquals(OutboxEvent.STATUS_PENDING, event.getStatus());
    assertNotNull(event.getPayload());

    verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));
  }

  @Test
  @DisplayName("Debe obtener eventos pendientes")
  void shouldReturnPendingEvents() {
    OutboxEvent event = OutboxEvent.builder()
        .id(UUID.randomUUID())
        .aggregateType("Usuario")
        .aggregateId(UUID.randomUUID())
        .eventType("UserRegistered")
        .payload("{}")
        .status(OutboxEvent.STATUS_PENDING)
        .retryCount(0)
        .build();

    when(outboxEventRepository.findTop50ByStatusOrderByFechaCreacionAsc(
        OutboxEvent.STATUS_PENDING))
        .thenReturn(List.of(event));

    List<OutboxEvent> result = outboxEventService.obtenerEventosPendientes();

    assertEquals(1, result.size());
    assertEquals(OutboxEvent.STATUS_PENDING, result.get(0).getStatus());
  }

  @Test
  @DisplayName("Debe marcar evento como publicado")
  void shouldMarkEventAsPublished() {
    UUID eventId = UUID.randomUUID();

    OutboxEvent event = OutboxEvent.builder()
        .id(eventId)
        .aggregateType("Usuario")
        .aggregateId(UUID.randomUUID())
        .eventType("UserRegistered")
        .payload("{}")
        .status(OutboxEvent.STATUS_PENDING)
        .retryCount(0)
        .build();

    when(outboxEventRepository.findById(eventId)).thenReturn(Optional.of(event));

    outboxEventService.marcarComoPublicado(eventId);

    assertEquals(OutboxEvent.STATUS_SENT, event.getStatus());
    assertNotNull(event.getFechaPublicacion());
    assertEquals(null, event.getErrorMessage());

    verify(outboxEventRepository, times(1)).save(event);
  }

  @Test
  @DisplayName("Debe marcar evento como fallido e incrementar reintentos")
  void shouldMarkEventAsFailed() {
    UUID eventId = UUID.randomUUID();

    OutboxEvent event = OutboxEvent.builder()
        .id(eventId)
        .aggregateType("Usuario")
        .aggregateId(UUID.randomUUID())
        .eventType("UserRegistered")
        .payload("{}")
        .status(OutboxEvent.STATUS_PENDING)
        .retryCount(0)
        .build();

    when(outboxEventRepository.findById(eventId)).thenReturn(Optional.of(event));

    outboxEventService.marcarComoFallido(eventId, "RabbitMQ no disponible");

    assertEquals(OutboxEvent.STATUS_FAILED, event.getStatus());
    assertEquals(1, event.getRetryCount());
    assertEquals("RabbitMQ no disponible", event.getErrorMessage());

    verify(outboxEventRepository, times(1)).save(event);
  }

  @Test
  @DisplayName("Debe lanzar excepción al marcar como publicado un evento inexistente")
  void shouldThrowExceptionWhenPublishedEventDoesNotExist() {
    UUID eventId = UUID.randomUUID();

    when(outboxEventRepository.findById(eventId)).thenReturn(Optional.empty());

    assertThrows(CredencialesInvalidasException.class, () ->
        outboxEventService.marcarComoPublicado(eventId)
    );
  }

  @Test
  @DisplayName("Debe lanzar excepción al marcar como fallido un evento inexistente")
  void shouldThrowExceptionWhenFailedEventDoesNotExist() {
    UUID eventId = UUID.randomUUID();

    when(outboxEventRepository.findById(eventId)).thenReturn(Optional.empty());

    assertThrows(CredencialesInvalidasException.class, () ->
        outboxEventService.marcarComoFallido(eventId, "Error")
    );
  }
}