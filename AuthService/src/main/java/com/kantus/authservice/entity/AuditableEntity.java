package com.kantus.authservice.entity;

import com.kantus.authservice.enums.EstadoRegistro;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Clase abstracta que proporciona los campos de auditoría transversales.
 * Todas las entidades del dominio deben heredar de esta clase para cumplir
 * con las métricas de trazabilidad y la norma ISO 25010.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

  @CreatedBy
  @Column(name = "creado_por", nullable = false, updatable = false)
  private UUID creadoPor;

  @CreatedDate
  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @LastModifiedBy
  @Column(name = "modificado_por")
  private UUID modificadoPor;

  @LastModifiedDate
  @Column(name = "fecha_modificacion")
  private LocalDateTime fechaModificacion;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_registro", nullable = false, length = 15)
  private EstadoRegistro estadoRegistro = EstadoRegistro.ACTIVO;

  /**
   * Hook del ciclo de vida de JPA.
   * Garantiza que el estado siempre sea ACTIVO por defecto antes de la inserción
   * si no fue asignado previamente.
   */
  @PrePersist
  protected void onPrePersist() {
    if (this.estadoRegistro == null) {
      this.estadoRegistro = EstadoRegistro.ACTIVO;
    }
  }
}