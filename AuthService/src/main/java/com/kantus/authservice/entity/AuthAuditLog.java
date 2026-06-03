package com.kantus.authservice.entity;

import com.kantus.authservice.enums.TipoEventoAuditoria;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

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
  private Usuario usuario; // Puede ser nulo si intentan loguearse con un usuario que no existe

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_evento", nullable = false, length = 50)
  private TipoEventoAuditoria tipoEvento;

  // JdbcTypeCode permite que Hibernate 6 guarde un String Java como un tipo JSONB nativo en PostgreSQL
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String detalle;

  @Column(name = "ip_origen", length = 45)
  private String ipOrigen;
}