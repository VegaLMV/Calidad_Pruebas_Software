package com.kantus.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Entidad paramétrica que define los perfiles de acceso en el sistema (Ej. ADMIN, CAJERO).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles", schema = "auth_db")
public class Rol extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String nombre;

  @Column(length = 255)
  private String descripcion;

  @Column(name = "es_sistema", nullable = false)
  private Boolean esSistema = false;
}