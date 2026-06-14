package com.kantus.authservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kantus.authservice.entity.OutboxEvent;
import com.kantus.authservice.exception.RecursoNoEncontradoException;
import com.kantus.authservice.repository.OutboxEventRepository;
import com.kantus.authservice.util.DateTimeProvider;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para registrar y administrar eventos del patrón Outbox.
 */
@Service
@RequiredArgsConstructor
public class OutboxEventService {

  private static final int MAX_ERROR_LENGTH = 1000;

  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  /**
   * Registra un evento outbox usando un payload JSON ya construido.
   *
   * @param aggregateType Tipo de agregado.
   * @param aggregateId Identificador del agregado.
   * @param eventType Tipo del evento.
   * @param payloadJson Payload JSON.
   * @return Evento registrado.
   */
  @Transactional
  public OutboxEvent registrarEvento(
      String aggregateType,
      UUID aggregateId,
      String eventType,
      String payloadJson
  ) {
    OutboxEvent event = OutboxEvent.builder()
        .aggregateType(aggregateType)
        .aggregateId(aggregateId)
        .eventType(eventType)
        .payload(payloadJson)
        .status(OutboxEvent.STATUS_PENDING)
        .retryCount(0)
        .fechaCreacion(DateTimeProvider.now())
        .build();

    return outboxEventRepository.save(event);
  }

  /**
   * Registra un evento outbox convirtiendo un mapa a JSON.
   *
   * @param aggregateType Tipo de agregado.
   * @param aggregateId Identificador del agregado.
   * @param eventType Tipo del evento.
   * @param payload Payload como mapa.
   * @return Evento registrado.
   */
  @Transactional
  public OutboxEvent registrarEvento(
      String aggregateType,
      UUID aggregateId,
      String eventType,
      Map<String, Object> payload
  ) {
    try {
      return registrarEvento(
          aggregateType,
          aggregateId,
          eventType,
          objectMapper.writeValueAsString(payload)
      );
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("No se pudo convertir el payload del evento a JSON", ex);
    }
  }

  /**
   * Obtiene eventos pendientes para su futura publicación en RabbitMQ.
   *
   * @return Lista de eventos pendientes.
   */
  @Transactional(readOnly = true)
  public List<OutboxEvent> obtenerEventosPendientes() {
    return outboxEventRepository.findTop50ByStatusOrderByFechaCreacionAsc(
        OutboxEvent.STATUS_PENDING
    );
  }

  /**
   * Marca un evento como publicado.
   *
   * @param eventId Identificador del evento.
   */
  @Transactional
  public void marcarComoPublicado(UUID eventId) {
    OutboxEvent event = buscarEvento(eventId);

    event.setStatus(OutboxEvent.STATUS_SENT);
    event.setFechaPublicacion(DateTimeProvider.now());
    event.setErrorMessage(null);

    outboxEventRepository.save(event);
  }

  /**
   * Marca un evento como fallido y aumenta el contador de reintentos.
   *
   * @param eventId Identificador del evento.
   * @param errorMessage Mensaje de error.
   */
  @Transactional
  public void marcarComoFallido(UUID eventId, String errorMessage) {
    OutboxEvent event = buscarEvento(eventId);

    event.setStatus(OutboxEvent.STATUS_FAILED);
    event.setRetryCount(event.getRetryCount() + 1);
    event.setErrorMessage(normalizarMensajeError(errorMessage));

    outboxEventRepository.save(event);
  }

  private OutboxEvent buscarEvento(UUID eventId) {
    return outboxEventRepository.findById(eventId)
        .orElseThrow(() ->
            new RecursoNoEncontradoException(
                "Evento outbox no encontrado: " + eventId
            )
        );
  }

  private String normalizarMensajeError(String errorMessage) {
    if (errorMessage == null) {
      return null;
    }

    if (errorMessage.length() <= MAX_ERROR_LENGTH) {
      return errorMessage;
    }

    return errorMessage.substring(0, MAX_ERROR_LENGTH);
  }
}