package com.kantus.customerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Representación de los clientes personas naturales del restaurante Kantus.
 * Esta entidad extiende de Cliente para heredar los atributos transversales
 * (ID, usuario_id) y la auditoría básica.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "clientes_naturales", schema = "customer_db")
@PrimaryKeyJoinColumn(name = "cliente_id") // Esta es la conexión física con la tabla 'clientes'
public class ClienteNatural extends Cliente {

  @Column(name = "nombres", nullable = false, length = 100)
  private String nombres;

  @Column(name = "apellidos", nullable = false, length = 100)
  private String apellidos;

  @Column(name = "tipo_documento", nullable = false, length = 10)
  private String tipoDocumento;

  @Column(name = "numero_documento", nullable = false, length = 20)
  private String numeroDocumento;

  @Column(name = "fecha_nacimiento")
  private java.time.LocalDate fechaNacimiento;
}