package com.kantus.authservice.repository;

import com.kantus.authservice.entity.Permiso;
import com.kantus.authservice.entity.Rol;
import com.kantus.authservice.entity.RolPermiso;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad RolPermiso que gestiona la relación entre roles y permisos.
 */
@Repository
public interface RolPermisoRepository extends JpaRepository<RolPermiso, UUID> {

  /**
   * Verifica si existe una relación entre un rol y un permiso específicos.
   *
   * @param rol El rol a verificar.
   * @param permiso El permiso a verificar.
   * @return true si la relación existe, false en caso contrario.
   */
  boolean existsByRolAndPermiso(Rol rol, Permiso permiso);

  /**
   * Obtiene los permisos asociados a una lista de roles.
   *
   * @param rolIds Identificadores de roles.
   * @return Lista de relaciones rol-permiso.
   */
  @EntityGraph(attributePaths = {"rol", "permiso"})
  List<RolPermiso> findByRolIdIn(Collection<UUID> rolIds);
}