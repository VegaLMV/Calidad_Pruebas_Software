package com.kantus.authservice.service;

import com.kantus.authservice.dto.request.EmpleadoRegistroRequest;
import com.kantus.authservice.dto.request.RegistroRequest;
import com.kantus.authservice.dto.response.MensajeResponse;
import com.kantus.authservice.entity.AuthAuditLog;
import com.kantus.authservice.entity.Rol;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.entity.UsuarioRol;
import com.kantus.authservice.enums.EstadoRegistro;
import com.kantus.authservice.enums.TipoEventoAuditoria;
import com.kantus.authservice.mapper.UsuarioMapper;
import com.kantus.authservice.repository.AuthAuditLogRepository;
import com.kantus.authservice.repository.RolRepository;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.repository.UsuarioRolRepository;
import com.kantus.authservice.util.DateTimeProvider;
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

  private static final String ROLE_CLIENTE = "ROLE_CLIENTE";

  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final UsuarioRolRepository usuarioRolRepository;
  private final PasswordEncoder passwordEncoder;
  private final UsuarioMapper usuarioMapper;
  private final AuthDomainEventService authDomainEventService;
  private final AuthAuditLogRepository auditLogRepository;

  /**
   * Flujo público: auto-registro de clientes.
   *
   * @param request Datos del nuevo cliente.
   * @return Mensaje de confirmación.
   */
  @Transactional
  public MensajeResponse registrarCliente(RegistroRequest request) {
    Usuario nuevoUsuario = procesarRegistroBase(
        request.getUsername(),
        request.getEmail(),
        request.getPassword()
    );

    Rol rolCliente = obtenerRolPorNombre(ROLE_CLIENTE);
    vincularRol(nuevoUsuario, rolCliente);

    authDomainEventService.registrarUserRegistered(nuevoUsuario);
    authDomainEventService.registrarUserRoleAssigned(nuevoUsuario, rolCliente);

    return new MensajeResponse("Cliente registrado exitosamente en el sistema Kantus", 201);
  }

  /**
   * Flujo privado: registro de empleados con rol específico.
   *
   * @param request Datos del nuevo empleado.
   * @return Mensaje de confirmación.
   */
  @Transactional
  public MensajeResponse registrarEmpleado(EmpleadoRegistroRequest request) {
    Usuario nuevoUsuario = procesarRegistroBase(
        request.getUsername(),
        request.getEmail(),
        request.getPassword()
    );

    Rol rolEmpleado = obtenerRolPorNombre(request.getRolAsignado());
    vincularRol(nuevoUsuario, rolEmpleado);

    authDomainEventService.registrarUserRegistered(nuevoUsuario);
    authDomainEventService.registrarUserRoleAssigned(nuevoUsuario, rolEmpleado);

    return new MensajeResponse(
        "Empleado registrado exitosamente con el rol: " + request.getRolAsignado(),
        201
    );
  }

  /**
   * Cambia el estado lógico de un usuario.
   *
   * @param username Nombre de usuario.
   * @param nuevoEstado Nuevo estado.
   * @return Mensaje de confirmación.
   */
  @Transactional
  public MensajeResponse cambiarEstado(String username, String nuevoEstado) {
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    try {
      EstadoRegistro estadoEnum = EstadoRegistro.valueOf(nuevoEstado.toUpperCase());
      usuario.setEstadoRegistro(estadoEnum);
      usuarioRepository.save(usuario);

      return new MensajeResponse(
          "El estado del usuario " + username + " ha sido cambiado a " + estadoEnum.name(),
          200
      );
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Estado no válido. Use 'ACTIVO' o 'INACTIVO'.");
    }
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

  private Rol obtenerRolPorNombre(String nombreRol) {
    return rolRepository.findByNombre(nombreRol)
        .orElseThrow(() -> new IllegalArgumentException(
            "El rol " + nombreRol + " no existe. Verifica la migración V3 de Flyway."));
  }

  private void vincularRol(Usuario usuario, Rol rol) {
    boolean yaTieneRol = usuarioRolRepository.existsByUsuarioIdAndRolId(
        usuario.getId(),
        rol.getId()
    );

    if (!yaTieneRol) {
      UsuarioRol usuarioRol = UsuarioRol.builder()
          .usuario(usuario)
          .rol(rol)
          .fechaAsignacion(DateTimeProvider.now())
          .build();

      usuarioRolRepository.save(usuarioRol);
    }
  }

  /**
   * Desbloquea una cuenta bloqueada por intentos fallidos.
   *
   * @param username Nombre de usuario.
   * @return Mensaje de confirmación.
   */
  @Transactional
  public MensajeResponse desbloquearUsuario(String username) {
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    usuario.setIntentosFallidos((short) 0);
    usuario.setBloqueadoHasta(null);
    usuarioRepository.save(usuario);

    AuthAuditLog auditLog = AuthAuditLog.builder()
        .usuario(usuario)
        .tipoEvento(TipoEventoAuditoria.DESBLOQUEO_CUENTA)
        .detalle("{\"motivo\": \"Cuenta desbloqueada por administrador\"}")
        .ipOrigen("SYSTEM")
        .build();

    auditLogRepository.save(auditLog);

    return new MensajeResponse("Usuario desbloqueado correctamente", 200);
  }
}