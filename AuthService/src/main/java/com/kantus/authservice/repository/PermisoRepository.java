package com.kantus.authservice.repository;

import com.kantus.authservice.entity.Permiso;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para la entidad Permiso que gestiona las operaciones de persistencia.
 */
public interface PermisoRepository extends JpaRepository<Permiso, UUID> {

  /**
   * Busca un permiso por su código identificador único.
   *
   * @param codigo El código del permiso a buscar.
   * @return Un Optional que contiene el permiso si es encontrado.
   */
  Optional<Permiso> findByCodigo(String codigo);
}