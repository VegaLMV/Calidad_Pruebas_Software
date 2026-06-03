package com.kantus.authservice.service;

import com.kantus.authservice.dto.request.EmpleadoRegistroRequest;
import com.kantus.authservice.dto.request.RegistroRequest;
import com.kantus.authservice.dto.response.MensajeResponse;
import com.kantus.authservice.entity.Rol;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.entity.UsuarioRol;
import com.kantus.authservice.mapper.UsuarioMapper;
import com.kantus.authservice.repository.RolRepository;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.repository.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final UsuarioRolRepository usuarioRolRepository;
  private final PasswordEncoder passwordEncoder;
  private final UsuarioMapper usuarioMapper;

  /**
   * Flujo Público: Auto-registro de clientes a través de la App/Web.
   * Siempre asigna ROLE_CLIENTE por seguridad.
   */
  @Transactional
  public MensajeResponse registrarCliente(RegistroRequest request) {
    Usuario nuevoUsuario = procesarRegistroBase(request.getUsername(), request.getEmail(), request.getPassword());

    // Asignación estricta de ROLE_CLIENTE
    Rol rolCliente = rolRepository.findByNombre("ROLE_CLIENTE").orElseGet(() ->
        rolRepository.save(Rol.builder().nombre("ROLE_CLIENTE").descripcion("Cliente regular del sistema").esSistema(false).build())
    );

    vincularRol(nuevoUsuario, rolCliente);
    return new MensajeResponse("Cliente registrado exitosamente en el sistema Kantus", 201);
  }

  /**
   * Flujo Privado: Registro de empleados por parte de un Administrador.
   * Permite asignar roles específicos (ROLE_CAJERO, ROLE_MOZO, etc.).
   */
  @Transactional
  public MensajeResponse registrarEmpleado(EmpleadoRegistroRequest request) {
    Usuario nuevoUsuario = procesarRegistroBase(request.getUsername(), request.getEmail(), request.getPassword());

    // Búsqueda del rol específico solicitado
    Rol rolEmpleado = rolRepository.findByNombre(request.getRolAsignado())
        .orElseThrow(() -> new IllegalArgumentException("El rol " + request.getRolAsignado() + " no existe en el sistema"));

    vincularRol(nuevoUsuario, rolEmpleado);
    return new MensajeResponse("Empleado registrado exitosamente con el rol: " + request.getRolAsignado(), 201);
  }

  // --- Métodos Privados Auxiliares para no repetir código ---

  private Usuario procesarRegistroBase(String username, String email, String password) {
    if (usuarioRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("El nombre de usuario ya está en uso");
    }
    if (usuarioRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("El correo electrónico ya está registrado");
    }
    String passwordHash = passwordEncoder.encode(password);

    RegistroRequest temporalRequest = new RegistroRequest(username, email, password);
    Usuario nuevoUsuario = usuarioMapper.toEntity(temporalRequest, passwordHash);

    return usuarioRepository.save(nuevoUsuario);
  }

  private void vincularRol(Usuario usuario, Rol rol) {
    UsuarioRol usuarioRol = UsuarioRol.builder()
        .usuario(usuario)
        .rol(rol)
        .fechaAsignacion(LocalDateTime.now())
        .build();
    usuarioRolRepository.save(usuarioRol);
  }
}