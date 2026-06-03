package com.kantus.authservice.service;

import com.kantus.authservice.dto.request.LoginRequest;
import com.kantus.authservice.dto.response.AuthResponse;
import com.kantus.authservice.entity.AuthAuditLog;
import com.kantus.authservice.entity.Sesion;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.enums.TipoEventoAuditoria;
import com.kantus.authservice.exception.CredencialesInvalidasException;
import com.kantus.authservice.exception.UsuarioBloqueadoException;
import com.kantus.authservice.repository.AuthAuditLogRepository;
import com.kantus.authservice.repository.SesionRepository;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.security.CustomUserDetailsService;
import com.kantus.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio encargado de gestionar la lógica de negocio para la autenticación de usuarios.
 * Aplica reglas de seguridad empresarial como bloqueo de cuentas, auditoría inmutable y sesiones.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UsuarioRepository usuarioRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final AuthAuditLogRepository auditLogRepository;
  private final SesionRepository sesionRepository;
  private final CustomUserDetailsService userDetailsService;

  /**
   * Procesa el intento de inicio de sesión de un usuario y genera la sesión.
   *
   * @param request DTO con las credenciales (username y password).
   * @param ipOrigen Dirección IP desde donde se realiza la petición (Para auditoría).
   * @param userAgent Navegador o dispositivo del cliente.
   * @return AuthResponse conteniendo el Token JWT y Refresh Token generados.
   */
  @Transactional
  public AuthResponse autenticarUsuario(LoginRequest request, String ipOrigen, String userAgent) {

    // 1. Buscar al usuario
    Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contraseña incorrectos"));

    // 2. Validar si la cuenta está bloqueada
    if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
      registrarAuditoria(usuario, TipoEventoAuditoria.LOGIN_FALLIDO, "Cuenta bloqueada temporalmente", ipOrigen);
      throw new UsuarioBloqueadoException("Su cuenta está bloqueada por múltiples intentos fallidos. Intente más tarde.");
    }

    try {
      // 3. Delegar la verificación de la contraseña a Spring Security (Bcrypt)
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
      );

      // 4. Resetear intentos fallidos y actualizar último login
      usuario.setIntentosFallidos((short) 0);
      usuario.setBloqueadoHasta(null);
      usuario.setUltimoLogin(LocalDateTime.now());
      usuarioRepository.save(usuario);

      // 5. Registrar el evento de éxito
      registrarAuditoria(usuario, TipoEventoAuditoria.LOGIN_EXITOSO, "Autenticación exitosa", ipOrigen);

      // 6. Generar el Token JWT (Corta duración) cargando los roles actualizados
      UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getId().toString());
      String token = jwtService.generateToken(userDetails);

      // 7. Generar y guardar Refresh Token (Larga duración) en la tabla Sesiones
      String refreshToken = UUID.randomUUID().toString();
      Sesion sesion = Sesion.builder()
          .usuario(usuario)
          .refreshTokenHash(refreshToken)
          .fechaExpiracion(LocalDateTime.now().plusDays(7)) // El Refresh Token dura 7 días
          .revocado(false)
          .ipOrigen(ipOrigen)
          .userAgent(userAgent)
          .build();
      sesionRepository.save(sesion);

      return AuthResponse.builder()
          .token(token)
          .refreshToken(refreshToken)
          .mensaje("Bienvenido al sistema Kantus")
          .build();

    } catch (BadCredentialsException ex) {
      manejarIntentoFallido(usuario, ipOrigen);
      throw new CredencialesInvalidasException("Usuario o contraseña incorrectos");
    }
  }

  /**
   * Valida un Refresh Token existente y emite un nuevo JWT si es válido.
   */
  @Transactional
  public AuthResponse refrescarToken(String refreshToken, String ipOrigen) {
    Sesion sesion = sesionRepository.findByRefreshTokenHash(refreshToken)
        .orElseThrow(() -> new CredencialesInvalidasException("Refresh Token inválido o no encontrado"));

    // Validar caducidad y revocación
    if (sesion.isRevocado() || sesion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
      throw new CredencialesInvalidasException("La sesión ha expirado o fue revocada. Inicie sesión nuevamente.");
    }

    // Emitir un nuevo JWT cargando los roles más recientes de la Base de Datos
    UserDetails userDetails = userDetailsService.loadUserByUsername(sesion.getUsuario().getId().toString());
    String nuevoToken = jwtService.generateToken(userDetails);

    return AuthResponse.builder()
        .token(nuevoToken)
        .refreshToken(refreshToken) // Se reutiliza el mismo refresh token hasta que caduque
        .mensaje("Token refrescado exitosamente")
        .build();
  }

  private void manejarIntentoFallido(Usuario usuario, String ipOrigen) {
    short intentos = (short) (usuario.getIntentosFallidos() + 1);
    usuario.setIntentosFallidos(intentos);

    String detalleAuditoria = "Intento fallido número: " + intentos;

    if (intentos >= 3) {
      usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
      detalleAuditoria = "Cuenta bloqueada tras 3 intentos fallidos";
    }

    usuarioRepository.save(usuario);
    registrarAuditoria(usuario, TipoEventoAuditoria.LOGIN_FALLIDO, detalleAuditoria, ipOrigen);
  }

  private void registrarAuditoria(Usuario usuario, TipoEventoAuditoria tipo, String detalle, String ip) {
    AuthAuditLog auditLog = AuthAuditLog.builder()
        .usuario(usuario)
        .tipoEvento(tipo)
        .detalle("{\"motivo\": \"" + detalle + "\"}")
        .ipOrigen(ip)
        .build();

    auditLogRepository.save(auditLog);
  }
}