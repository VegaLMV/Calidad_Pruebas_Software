package com.kantus.authservice.entity;

import com.kantus.authservice.enums.TipoEventoAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Bitácora inmutable de eventos de seguridad.
 * Registra intentos de hackeo o accesos exitosos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_audit_log", schema = "auth_db")
public class AuthAuditLog extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_evento", nullable = false, length = 50)
  private TipoEventoAuditoria tipoEvento;

  /**
   * JdbcTypeCode permite que Hibernate 6 guarde un String Java
   * como un tipo JSONB nativo en PostgreSQL.
   */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String detalle;

  @Column(name = "ip_origen", length = 45)
  private String ipOrigen;
}