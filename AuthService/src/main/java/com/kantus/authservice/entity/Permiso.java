package com.kantus.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Catálogo de permisos granulares del sistema.
 * Formato de código esperado: "recurso:accion" (Ej. "order:create").
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "permisos", schema = "auth_db")
public class Permiso extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String codigo;

  @Column(nullable = false, length = 50)
  private String modulo;

  @Column(length = 255)
  private String descripcion;
}