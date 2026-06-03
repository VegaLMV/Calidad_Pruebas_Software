package com.kantus.authservice.repository;

import com.kantus.authservice.entity.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de la entidad Usuario en la base de datos.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

  /**
   * Busca un usuario por su nombre de usuario.
   *
   * @param username El nombre de usuario a buscar.
   * @return Un Optional que contiene el usuario si es encontrado.
   */
  Optional<Usuario> findByUsername(String username);

  /**
   * Verifica si existe un usuario registrado con el nombre de usuario dado.
   *
   * @param username El nombre de usuario a verificar.
   * @return True si existe, false en caso contrario.
   */
  boolean existsByUsername(String username);

  /**
   * Verifica si existe un usuario registrado con el correo electrónico dado.
   *
   * @param email El correo electrónico a verificar.
   * @return True si existe, false en caso contrario.
   */
  boolean existsByEmail(String email);
}