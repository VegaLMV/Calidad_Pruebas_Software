package com.kantus.authservice.mapper;

import com.kantus.authservice.dto.request.RegistroRequest;
import com.kantus.authservice.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

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