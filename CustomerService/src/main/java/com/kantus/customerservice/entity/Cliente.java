package com.kantus.customerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entidad base para todos los clientes (Naturales y Empresas).
 * Mantiene la relación lógica con el microservicio de autenticación.
 * Extiende de AuditableEntity para cumplir con la norma ISO 25010.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "clientes", schema = "customer_db")
@Inheritance(strategy = InheritanceType.JOINED)
public class Cliente extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "usuario_id", nullable = false, unique = true)
  private UUID usuarioId;

  @Column(name = "tipo_cliente", nullable = false, length = 50)
  private String tipoCliente;

  @Column(name = "email_contacto", nullable = false, length = 255)
  private String emailContacto;

  @Column(name = "telefono_contacto", length = 20)
  private String telefonoContacto;

  @Column(name = "acepta_marketing", nullable = false)
  private Boolean aceptaMarketing = false;
}