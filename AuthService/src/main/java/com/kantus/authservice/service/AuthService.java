package com.kantus.authservice.service;

import com.kantus.authservice.dto.request.ChangePasswordRequest;
import com.kantus.authservice.dto.request.LoginRequest;
import com.kantus.authservice.dto.request.LogoutRequest;
import com.kantus.authservice.dto.response.AuthMeResponse;
import com.kantus.authservice.dto.response.AuthResponse;
import com.kantus.authservice.dto.response.MensajeResponse;
import com.kantus.authservice.dto.response.TokenValidationResponse;
import com.kantus.authservice.entity.AuthAuditLog;
import com.kantus.authservice.entity.RolPermiso;
import com.kantus.authservice.entity.Sesion;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.entity.UsuarioRol;
import com.kantus.authservice.enums.TipoEventoAuditoria;
import com.kantus.authservice.exception.CredencialesInvalidasException;
import com.kantus.authservice.exception.UsuarioBloqueadoException;
import com.kantus.authservice.repository.AuthAuditLogRepository;
import com.kantus.authservice.repository.RolPermisoRepository;
import com.kantus.authservice.repository.SesionRepository;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.repository.UsuarioRolRepository;
import com.kantus.authservice.security.CustomUserDetailsService;
import com.kantus.authservice.security.JwtService;
import com.kantus.authservice.util.DateTimeProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de gestionar la lógica de negocio para la autenticación de usuarios.
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
  private final UsuarioRolRepository usuarioRolRepository;
  private final RolPermisoRepository rolPermisoRepository;
  private final PasswordEncoder passwordEncoder;
  private static final String CLAIM_USERNAME = "username";
  private static final String CLAIM_ROLES = "roles";
  private static final String CLAIM_PERMISSIONS = "permissions";

  /**
   * Procesa el inicio de sesión y genera la sesión.
   *
   * @param request Datos de acceso.
   * @param ipOrigen IP del cliente.
   * @param userAgent Dispositivo.
   * @return AuthResponse con tokens.
   */
  @Transactional
  public AuthResponse autenticarUsuario(LoginRequest request, String ipOrigen, String userAgent) {
    Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new CredencialesInvalidasException(
            "Usuario o contraseña incorrectos"));

    if (usuario.getBloqueadoHasta() != null
        && usuario.getBloqueadoHasta().isAfter(DateTimeProvider.now())) {
      registrarAuditoria(
          usuario,
          TipoEventoAuditoria.LOGIN_FALLIDO,
          "Cuenta bloqueada temporalmente",
          ipOrigen
      );
      throw new UsuarioBloqueadoException("Su cuenta está bloqueada. Intente más tarde.");
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
      );

      usuario.setIntentosFallidos((short) 0);
      usuario.setBloqueadoHasta(null);
      usuario.setUltimoLogin(DateTimeProvider.now());
      usuarioRepository.save(usuario);

      registrarAuditoria(
          usuario,
          TipoEventoAuditoria.LOGIN_EXITOSO,
          "Autenticación exitosa",
          ipOrigen
      );

      UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getId().toString());
      String token = jwtService.generateToken(construirClaimsJwt(usuario), userDetails);
      String refreshToken = UUID.randomUUID().toString();

      Sesion sesion = Sesion.builder()
          .usuario(usuario)
          .refreshTokenHash(refreshToken)
          .fechaExpiracion(DateTimeProvider.now().plusDays(7))
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
   *
   * @param refreshToken Refresh token actual.
   * @param ipOrigen IP de origen.
   * @return Nuevo JWT con el mismo refresh token.
   */
  @Transactional
  public AuthResponse refrescarToken(String refreshToken, String ipOrigen) {
    Sesion sesion = sesionRepository.findByRefreshTokenHash(refreshToken)
        .orElseThrow(() -> new CredencialesInvalidasException("Token inválido"));

    if (sesion.isRevocado() || sesion.getFechaExpiracion().isBefore(DateTimeProvider.now())) {
      throw new CredencialesInvalidasException("La sesión ha expirado o fue revocada.");
    }

    Usuario usuario = sesion.getUsuario();
    UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getId().toString());
    String nuevoToken = jwtService.generateToken(construirClaimsJwt(usuario), userDetails);

    return AuthResponse.builder()
        .token(nuevoToken)
        .refreshToken(refreshToken)
        .mensaje("Token refrescado exitosamente")
        .build();
  }

  private Map<String, Object> construirClaimsJwt(Usuario usuario) {
    List<UsuarioRol> usuarioRoles = usuarioRolRepository.findByUsuarioId(usuario.getId());

    List<UUID> rolIds = usuarioRoles.stream()
        .map(usuarioRol -> usuarioRol.getRol().getId())
        .toList();

    List<String> roles = usuarioRoles.stream()
        .map(usuarioRol -> normalizarRol(usuarioRol.getRol().getNombre()))
        .distinct()
        .sorted()
        .toList();

    List<String> permissions = rolIds.isEmpty()
        ? List.of()
        : rolPermisoRepository.findByRolIdIn(rolIds).stream()
        .map(RolPermiso::getPermiso)
        .map(permiso -> permiso.getCodigo())
        .distinct()
        .sorted()
        .toList();

    Map<String, Object> claims = new HashMap<>();
    claims.put(CLAIM_USERNAME, usuario.getUsername());
    claims.put(CLAIM_ROLES, roles);
    claims.put(CLAIM_PERMISSIONS, permissions);

    return claims;
  }

  private String normalizarRol(String nombreRol) {
    if (nombreRol == null) {
      return "";
    }

    return nombreRol.startsWith("ROLE_")
        ? nombreRol.substring(5)
        : nombreRol;
  }

  private void manejarIntentoFallido(Usuario usuario, String ipOrigen) {
    short intentos = (short) (usuario.getIntentosFallidos() + 1);
    usuario.setIntentosFallidos(intentos);

    String detalle = "Intento fallido número: " + intentos;

    if (intentos >= 3) {
      usuario.setBloqueadoHasta(DateTimeProvider.now().plusMinutes(15));
      detalle = "Cuenta bloqueada tras 3 intentos fallidos";
    }

    usuarioRepository.save(usuario);
    registrarAuditoria(usuario, TipoEventoAuditoria.LOGIN_FALLIDO, detalle, ipOrigen);
  }

  private void registrarAuditoria(
      Usuario usuario,
      TipoEventoAuditoria tipo,
      String detalle,
      String ip
  ) {
    AuthAuditLog auditLog = AuthAuditLog.builder()
        .usuario(usuario)
        .tipoEvento(tipo)
        .detalle("{\"motivo\": \"" + detalle + "\"}")
        .ipOrigen(ip)
        .build();

    auditLogRepository.save(auditLog);
  }

  /**
   * Cierra sesión revocando el refresh token.
   *
   * @param request Refresh token a revocar.
   * @param ipOrigen IP de origen.
   * @return Mensaje de confirmación.
   */
  @Transactional
  public MensajeResponse cerrarSesion(LogoutRequest request, String ipOrigen) {
    Sesion sesion = sesionRepository.findByRefreshTokenHash(request.getRefreshToken())
        .orElseThrow(() -> new CredencialesInvalidasException("Refresh token inválido"));

    if (!sesion.isRevocado()) {
      sesion.setRevocado(true);
      sesionRepository.save(sesion);

      registrarAuditoria(
          sesion.getUsuario(),
          TipoEventoAuditoria.TOKEN_REVOCADO,
          "Sesión cerrada correctamente",
          ipOrigen
      );
    }

    return new MensajeResponse("Sesión cerrada correctamente", 200);
  }

  /**
   * Obtiene información del usuario autenticado.
   *
   * @param usuarioIdTexto UUID del usuario autenticado.
   * @return Datos del usuario autenticado.
   */
  @Transactional(readOnly = true)
  public AuthMeResponse obtenerUsuarioAutenticado(String usuarioIdTexto) {
    Usuario usuario = obtenerUsuarioPorId(usuarioIdTexto);
    Map<String, Object> claims = construirClaimsJwt(usuario);

    return AuthMeResponse.builder()
        .id(usuario.getId())
        .username(usuario.getUsername())
        .email(usuario.getEmail())
        .roles(extraerLista(claims, CLAIM_ROLES))
        .permissions(extraerLista(claims, CLAIM_PERMISSIONS))
        .requiereCambioPassword(Boolean.TRUE.equals(usuario.getRequiereCambioPassword()))
        .mfaHabilitado(Boolean.TRUE.equals(usuario.getMfaHabilitado()))
        .build();
  }

  /**
   * Valida el token actual. Si el endpoint se ejecuta, el JWT ya fue aceptado
   * por el filtro de seguridad.
   *
   * @param usuarioIdTexto UUID del usuario autenticado.
   * @return Resultado de validación.
   */
  @Transactional(readOnly = true)
  public TokenValidationResponse validarTokenActual(String usuarioIdTexto) {
    Usuario usuario = obtenerUsuarioPorId(usuarioIdTexto);
    Map<String, Object> claims = construirClaimsJwt(usuario);

    return TokenValidationResponse.builder()
        .valid(true)
        .userId(usuario.getId())
        .username(usuario.getUsername())
        .roles(extraerLista(claims, "roles"))
        .permissions(extraerLista(claims, "permissions"))
        .build();
  }

  /**
   * Cambia la contraseña del usuario autenticado.
   *
   * @param usuarioIdTexto UUID del usuario autenticado.
   * @param request Datos de cambio de contraseña.
   * @param ipOrigen IP de origen.
   * @return Mensaje de confirmación.
   */
  @Transactional
  public MensajeResponse cambiarPassword(
      String usuarioIdTexto,
      ChangePasswordRequest request,
      String ipOrigen
  ) {
    Usuario usuario = obtenerUsuarioPorId(usuarioIdTexto);

    if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPasswordHash())) {
      registrarAuditoria(
          usuario,
          TipoEventoAuditoria.LOGIN_FALLIDO,
          "Contraseña actual incorrecta en cambio de contraseña",
          ipOrigen
      );
      throw new CredencialesInvalidasException("La contraseña actual es incorrecta");
    }

    usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    usuario.setRequiereCambioPassword(false);
    usuarioRepository.save(usuario);

    revocarSesionesActivas(usuario.getId());

    registrarAuditoria(
        usuario,
        TipoEventoAuditoria.CAMBIO_PASSWORD,
        "Contraseña actualizada correctamente",
        ipOrigen
    );

    return new MensajeResponse("Contraseña actualizada correctamente", 200);
  }

  private Usuario obtenerUsuarioPorId(String usuarioIdTexto) {
    try {
      UUID usuarioId = UUID.fromString(usuarioIdTexto);

      return usuarioRepository.findById(usuarioId)
          .orElseThrow(() -> new CredencialesInvalidasException(
              "Usuario autenticado no encontrado"));
    } catch (IllegalArgumentException ex) {
      throw new CredencialesInvalidasException(
          "Token inválido: identificador de usuario incorrecto");
    }
  }

  private void revocarSesionesActivas(UUID usuarioId) {
    List<Sesion> sesionesActivas = sesionRepository.findByUsuarioIdAndRevocadoFalse(usuarioId);

    sesionesActivas.forEach(sesion -> sesion.setRevocado(true));

    sesionRepository.saveAll(sesionesActivas);
  }

  @SuppressWarnings("unchecked")
  private List<String> extraerLista(Map<String, Object> claims, String key) {
    Object value = claims.get(key);

    if (value instanceof List<?>) {
      return (List<String>) value;
    }

    return List.of();
  }
}