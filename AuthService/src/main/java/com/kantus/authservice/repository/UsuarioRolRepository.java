package com.kantus.authservice.repository;

import com.kantus.authservice.entity.UsuarioRol;
import java.util.List;
import java.util.UUID;
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
  List<UsuarioRol> findByUsuarioId(UUID usuarioId);
}