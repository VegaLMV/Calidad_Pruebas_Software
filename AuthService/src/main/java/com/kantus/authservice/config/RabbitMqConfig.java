package com.kantus.authservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración de RabbitMQ para publicar eventos del AuthService.
 */
@Configuration
@EnableScheduling
public class RabbitMqConfig {

  public static final String KANTUS_EVENTS_EXCHANGE = "kantus.events.exchange";
  public static final String AUTH_EVENTS_QUEUE = "auth.events.queue";
  public static final String AUTH_ROUTING_PATTERN = "auth.#";

  /**
   * Exchange principal de eventos del ecosistema Kantus.
   *
   * @return TopicExchange configurado.
   */
  @Bean
  public TopicExchange kantusEventsExchange() {
    return new TopicExchange(KANTUS_EVENTS_EXCHANGE, true, false);
  }

  /**
   * Cola local para eventos emitidos por AuthService.
   *
   * @return Cola durable.
   */
  @Bean
  public Queue authEventsQueue() {
    return new Queue(AUTH_EVENTS_QUEUE, true);
  }

  /**
   * Binding que enruta eventos auth.* hacia la cola de AuthService.
   *
   * @param authEventsQueue Cola de eventos.
   * @param kantusEventsExchange Exchange principal.
   * @return Binding configurado.
   */
  @Bean
  public Binding authEventsBinding(
      Queue authEventsQueue,
      TopicExchange kantusEventsExchange
  ) {
    return BindingBuilder
        .bind(authEventsQueue)
        .to(kantusEventsExchange)
        .with(AUTH_ROUTING_PATTERN);
  }
}