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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de gestionar la lógica de negocio para la autenticación de usuarios.
 * Aplica reglas de seguridad empresarial como bloqueo de cuentas y auditoría.
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
   * Procesa el inicio de sesión y genera la sesión.
   *
   * @param request Datos de acceso
   * @param ipOrigen IP del cliente
   * @param userAgent Dispositivo
   * @return AuthResponse con tokens
   */
  @Transactional
  public AuthResponse autenticarUsuario(LoginRequest request, String ipOrigen, String userAgent) {

    Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contraseña incorrectos"));

    if (usuario.getBloqueadoHasta() != null
        && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
      registrarAuditoria(usuario, TipoEventoAuditoria.LOGIN_FALLIDO,
          "Cuenta bloqueada temporalmente", ipOrigen);
      throw new UsuarioBloqueadoException("Su cuenta está bloqueada. Intente más tarde.");
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
      );

      usuario.setIntentosFallidos((short) 0);
      usuario.setBloqueadoHasta(null);
      usuario.setUltimoLogin(LocalDateTime.now());
      usuarioRepository.save(usuario);

      registrarAuditoria(usuario, TipoEventoAuditoria.LOGIN_EXITOSO,
          "Autenticación exitosa", ipOrigen);

      UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getId().toString());
      String token = jwtService.generateToken(userDetails);

      String refreshToken = UUID.randomUUID().toString();
      Sesion sesion = Sesion.builder()
          .usuario(usuario)
          .refreshTokenHash(refreshToken)
          .fechaExpiracion(LocalDateTime.now().plusDays(7))
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
        .orElseThrow(() -> new CredencialesInvalidasException("Token inválido"));

    if (sesion.isRevocado() || sesion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
      throw new CredencialesInvalidasException("La sesión ha expirado o fue revocada.");
    }

    UserDetails userDetails = userDetailsService
        .loadUserByUsername(sesion.getUsuario().getId().toString());
    String nuevoToken = jwtService.generateToken(userDetails);

    return AuthResponse.builder()
        .token(nuevoToken)
        .refreshToken(refreshToken)
        .mensaje("Token refrescado exitosamente")
        .build();
  }

  private void manejarIntentoFallido(Usuario usuario, String ipOrigen) {
    short intentos = (short) (usuario.getIntentosFallidos() + 1);
    usuario.setIntentosFallidos(intentos);

    String detalle = "Intento fallido número: " + intentos;
    if (intentos >= 3) {
      usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
      detalle = "Cuenta bloqueada tras 3 intentos fallidos";
    }

    usuarioRepository.save(usuario);
    registrarAuditoria(usuario, TipoEventoAuditoria.LOGIN_FALLIDO, detalle, ipOrigen);
  }

  private void registrarAuditoria(Usuario usuario, TipoEventoAuditoria tipo,
                                  String detalle, String ip) {
    AuthAuditLog auditLog = AuthAuditLog.builder()
        .usuario(usuario)
        .tipoEvento(tipo)
        .detalle("{\"motivo\": \"" + detalle + "\"}")
        .ipOrigen(ip)
        .build();
    auditLogRepository.save(auditLog);
  }
}