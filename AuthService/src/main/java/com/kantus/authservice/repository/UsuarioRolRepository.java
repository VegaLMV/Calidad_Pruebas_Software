package com.kantus.authservice.repository;

import com.kantus.authservice.entity.UsuarioRol;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de la entidad UsuarioRol en la base de datos.
 */
@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UUID> {

  /**
   * Obtiene la lista de roles asignados a un usuario específico.
   *
   * @param usuarioId El UUID del usuario.
   * @return Lista de relaciones UsuarioRol encontradas.
   */
  @EntityGraph(attributePaths = {"rol"})
  List<UsuarioRol> findByUsuarioId(UUID usuarioId);

  /**
   * Verifica si un usuario ya tiene asignado un rol específico.
   *
   * @param usuarioId Identificador del usuario.
   * @param rolId Identificador del rol.
   * @return true si la relación usuario-rol ya existe.
   */
  boolean existsByUsuarioIdAndRolId(UUID usuarioId, UUID rolId);
}