package com.kantus.authservice.service;

import com.kantus.authservice.dto.request.EmpleadoRegistroRequest;
import com.kantus.authservice.dto.request.RegistroRequest;
import com.kantus.authservice.dto.response.MensajeResponse;
import com.kantus.authservice.entity.Rol;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.entity.UsuarioRol;
import com.kantus.authservice.enums.EstadoRegistro;
import com.kantus.authservice.mapper.UsuarioMapper;
import com.kantus.authservice.repository.RolRepository;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.repository.UsuarioRolRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de la lógica de negocio para la gestión y registro de usuarios.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final UsuarioRolRepository usuarioRolRepository;
  private final PasswordEncoder passwordEncoder;
  private final UsuarioMapper usuarioMapper;

  /**
   * Flujo Público: Auto-registro de clientes. Asigna ROLE_CLIENTE por defecto.
   *
   * @param request Datos del nuevo cliente.
   * @return Mensaje de confirmación.
   */
  @Transactional
  public MensajeResponse registrarCliente(RegistroRequest request) {
    Usuario nuevoUsuario = procesarRegistroBase(request.getUsername(), request.getEmail(),
        request.getPassword());

    Rol rolCliente = rolRepository.findByNombre("ROLE_CLIENTE").orElseGet(() ->
        rolRepository.save(Rol.builder()
            .nombre("ROLE_CLIENTE")
            .descripcion("Cliente regular del sistema")
            .esSistema(false)
            .build()));

    vincularRol(nuevoUsuario, rolCliente);
    return new MensajeResponse("Cliente registrado exitosamente en el sistema Kantus", 201);
  }

  /**
   * Flujo Privado: Registro de empleados. Permite asignar roles específicos.
   *
   * @param request Datos del nuevo empleado.
   * @return Mensaje de confirmación.
   */
  @Transactional
  public MensajeResponse registrarEmpleado(EmpleadoRegistroRequest request) {
    Usuario nuevoUsuario = procesarRegistroBase(request.getUsername(), request.getEmail(),
        request.getPassword());

    Rol rolEmpleado = rolRepository.findByNombre(request.getRolAsignado())
        .orElseThrow(() -> new IllegalArgumentException(
            "El rol " + request.getRolAsignado() + " no existe en el sistema"));

    vincularRol(nuevoUsuario, rolEmpleado);
    return new MensajeResponse("Empleado registrado exitosamente con el rol: "
        + request.getRolAsignado(), 201);
  }

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

  /**
   * Cambia el estado (ACTIVO/INACTIVO) de un usuario en la base de datos.
   *
   * @param username El nombre de usuario.
   * @param nuevoEstado El estado en formato String (ej: "INACTIVO").
   * @return Mensaje de confirmación.
   */
  @Transactional
  public MensajeResponse cambiarEstado(String username, String nuevoEstado) {
    // 1. Buscar al usuario
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    try {
      // 2. Convertir el String recibido al Enum
      EstadoRegistro estadoEnum = EstadoRegistro.valueOf(nuevoEstado.toUpperCase());

      // 3. Asignar el nuevo estado (Heredado de AuditableEntity)
      usuario.setEstadoRegistro(estadoEnum);
      usuarioRepository.save(usuario);

      return new MensajeResponse("El estado del usuario " + username
          + " ha sido cambiado a " + estadoEnum.name(), 200);

    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Estado no válido. Use 'ACTIVO' o 'INACTIVO'.");
    }
  }
}