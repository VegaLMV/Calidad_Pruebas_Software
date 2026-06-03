package com.kantus.authservice.repository;

import com.kantus.authservice.entity.Rol;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de la entidad Rol en la base de datos.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {

  /**
   * Busca un rol por su nombre único.
   *
   * @param nombre El nombre del rol (ej. "ROLE_ADMIN").
   * @return Un Optional que contiene el rol si se encuentra.
   */
  Optional<Rol> findByNombre(String nombre);
}