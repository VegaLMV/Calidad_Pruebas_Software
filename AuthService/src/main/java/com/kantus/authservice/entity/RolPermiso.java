package com.kantus.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Mapea qué permisos exactos tiene cada Rol (Control de Acceso Basado en Roles - RBAC).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "rol_permisos",
    schema = "auth_db",
    uniqueConstraints = @UniqueConstraint(columnNames = {"rol_id", "permiso_id"})
)
public class RolPermiso extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rol_id", nullable = false)
  private Rol rol;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permiso_id", nullable = false)
  private Permiso permiso;
}