package com.kantus.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa la raíz de identidad de un usuario (Cliente, Mozo, Admin).
 * No contiene datos comerciales, solo credenciales y estado de seguridad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuarios", schema = "auth_db")
public class Usuario extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "requiere_cambio_password", nullable = false)
  private Boolean requiereCambioPassword = false;

  @Column(name = "mfa_habilitado", nullable = false)
  private Boolean mfaHabilitado = false;

  @Column(name = "ultimo_login")
  private LocalDateTime ultimoLogin;

  @Column(name = "intentos_fallidos", nullable = false)
  private Short intentosFallidos = 0;

  @Column(name = "bloqueado_hasta")
  private LocalDateTime bloqueadoHasta;
}