package com.kantus.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controla las sesiones activas en dispositivos para permitir cierres de sesión remotos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sesiones", schema = "auth_db")
public class Sesion extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(name = "refresh_token_hash", nullable = false, length = 255)
  private String refreshTokenHash;

  @Column(name = "ip_origen", length = 45)
  private String ipOrigen;

  @Column(name = "user_agent", length = 255)
  private String userAgent;

  @Column(name = "fecha_expiracion", nullable = false)
  private LocalDateTime fechaExpiracion;

  @Column(nullable = false)
  private boolean revocado = false;
}