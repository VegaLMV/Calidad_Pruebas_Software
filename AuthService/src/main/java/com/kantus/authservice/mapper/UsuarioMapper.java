package com.kantus.authservice.mapper;

import com.kantus.authservice.dto.request.RegistroRequest;
import com.kantus.authservice.entity.Usuario;
import org.springframework.stereotype.Component;

/**
 * Mapper para la conversión entre objetos DTO de usuario y entidades del sistema.
 */
@Component
public class UsuarioMapper {

  /**
   * Convierte un objeto RegistroRequest en una entidad Usuario persistente.
   *
   * @param request Datos del registro del usuario.
   * @param passwordHash Hash de la contraseña ya cifrada.
   * @return La entidad Usuario construida.
   */
  public Usuario toEntity(RegistroRequest request, String passwordHash) {
    return Usuario.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .passwordHash(passwordHash)
        .requiereCambioPassword(false)
        .mfaHabilitado(false)
        .intentosFallidos((short) 0)
        .build();
  }
}