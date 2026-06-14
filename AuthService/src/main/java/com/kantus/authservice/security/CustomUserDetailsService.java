package com.kantus.authservice.security;

import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.entity.UsuarioRol;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.repository.UsuarioRolRepository;
import com.kantus.authservice.util.DateTimeProvider;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
      // Intentamos convertir la entrada a UUID (Cuando el JwtFilter lee el token)
      UUID id = UUID.fromString(input);
      usuario = usuarioRepository.findById(id)
          .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado por ID: " + id));
    } catch (IllegalArgumentException e) {
      // Si da error, es texto normal (Cuando el usuario hace Login)
      usuario = usuarioRepository.findByUsername(input)
          .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + input));
    }

    // Buscamos roles
    List<UsuarioRol> rolesDelUsuario = usuarioRolRepository.findByUsuarioId(usuario.getId());

    // Mapeamos roles para Spring Security
    List<SimpleGrantedAuthority> authorities = rolesDelUsuario.stream()
        .map(ur -> new SimpleGrantedAuthority(ur.getRol().getNombre()))
        .toList();

    // Retornamos el objeto final
    return User.builder()
        .username(usuario.getId().toString())
        .password(usuario.getPasswordHash())
        .authorities(authorities)
        .accountLocked(usuario.getBloqueadoHasta() != null
            && usuario.getBloqueadoHasta().isAfter(DateTimeProvider.now()))
        .build();
  }
}