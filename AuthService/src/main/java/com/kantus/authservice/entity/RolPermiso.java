package com.kantus.authservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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