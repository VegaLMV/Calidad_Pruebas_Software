package com.kantus.authservice.repository;

import com.kantus.authservice.entity.Sesion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de la entidad Sesion en la base de datos.
 */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, UUID> {

  /**
   * Busca una sesión activa mediante el hash de su token de refresco.
   *
   * @param refreshTokenHash El hash del token de refresco.
   * @return Un Optional que contiene la sesión si existe.
   */
  Optional<Sesion> findByRefreshTokenHash(String refreshTokenHash);

  /**
   * Lista las sesiones activas no revocadas de un usuario.
   *
   * @param usuarioId Identificador del usuario.
   * @return Lista de sesiones activas del usuario.
   */
  List<Sesion> findByUsuarioIdAndRevocadoFalse(UUID usuarioId);

}