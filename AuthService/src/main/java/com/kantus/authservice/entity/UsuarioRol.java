package com.kantus.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapea la relación Muchos a Muchos entre Usuarios y Roles,
 * permitiendo la trazabilidad (Auditoría) de cuándo se asignó el rol.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuario_roles", schema = "auth_db")
public class UsuarioRol extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rol_id", nullable = false)
  private Rol rol;

  @Column(name = "fecha_asignacion", nullable = false)
  private LocalDateTime fechaAsignacion;
}