package com.kantus.authservice.security;

import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.entity.UsuarioRol;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.repository.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación personalizada para cargar los datos y ROLES del usuario.
 * Adaptada para soportar búsqueda por Username (Login) y por UUID (Token JWT).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UsuarioRepository usuarioRepository;
  private final UsuarioRolRepository usuarioRolRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
    Usuario usuario;

    // LÓGICA INTELIGENTE: ¿Es un UUID o un Username normal?
    try {
      // Intentamos convertir la entrada a UUID (Esto ocurre cuando el JwtFilter lee el token)
      UUID id = UUID.fromString(input);
      usuario = usuarioRepository.findById(id)
          .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado por ID: " + id));
    } catch (IllegalArgumentException e) {
      // Si da error, significa que es texto normal (Esto ocurre cuando el usuario hace Login)
      usuario = usuarioRepository.findByUsername(input)
          .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado por username: " + input));
    }

    // Buscamos roles
    List<UsuarioRol> rolesDelUsuario = usuarioRolRepository.findByUsuarioId(usuario.getId());

    // Mapeamos roles para Spring Security
    List<SimpleGrantedAuthority> authorities = rolesDelUsuario.stream()
        .map(usuarioRol -> new SimpleGrantedAuthority(usuarioRol.getRol().getNombre()))
        .collect(Collectors.toList());

    // Retornamos el objeto final
    return User.builder()
        .username(usuario.getId().toString()) // Mantenemos el UUID como username interno para Auditoría
        .password(usuario.getPasswordHash())
        .authorities(authorities)
        .accountLocked(usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(java.time.LocalDateTime.now()))
        .build();
  }
}