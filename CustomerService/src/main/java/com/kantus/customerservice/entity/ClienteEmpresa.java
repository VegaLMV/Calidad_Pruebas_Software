package com.kantus.customerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representación de los clientes corporativos o empresas.
 * Mantiene la integridad con la tabla principal de clientes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "clientes_empresas")
@PrimaryKeyJoinColumn(name = "id")
public class ClienteEmpresa extends Cliente {

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(name = "nombre_comercial", length = 150)
    private String nombreComercial;

    @Column(name = "contacto_principal", length = 150)
    private String contactoPrincipal;
}