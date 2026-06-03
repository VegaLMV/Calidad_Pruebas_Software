package com.kantus.authservice.controller;

import com.kantus.authservice.dto.request.LoginRequest;
import com.kantus.authservice.dto.request.RefreshTokenRequest;
import com.kantus.authservice.dto.response.AuthResponse;
import com.kantus.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST encargado de exponer los endpoints de seguridad.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Seguridad y Accesos",
    description = "Endpoints para inicio de sesión, tokens y control de identidad")
public class AuthController {

  private final AuthService authService;

  /**
   * Autentica al usuario en el sistema.
   *
   * @param request Datos de acceso
   * @param httpRequest Solicitud HTTP para extraer IP y User-Agent
   * @return Token de acceso y Refresh Token
   */
  @PostMapping("/login")
  @Operation(summary = "Autenticar Usuario",
      description = "Valida credenciales, emite JWT y registra la sesión con un Refresh Token.")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest) {

    String ipOrigen = extraerIpReal(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    AuthResponse response = authService.autenticarUsuario(request, ipOrigen, userAgent);

    return ResponseEntity.ok(response);
  }

  /**
   * Genera un nuevo par de tokens.
   *
   * @param request Request que contiene el refresh token previo
   * @param httpRequest Solicitud HTTP
   * @return Nuevo AuthResponse
   */
  @PostMapping("/refresh-token")
  @Operation(summary = "Refrescar Token JWT",
      description = "Genera un nuevo JWT usando un Refresh Token válido.")
  public ResponseEntity<AuthResponse> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request,
      HttpServletRequest httpRequest) {

    String ipOrigen = extraerIpReal(httpRequest);
    AuthResponse response = authService.refrescarToken(request.getRefreshToken(), ipOrigen);

    return ResponseEntity.ok(response);
  }

  private String extraerIpReal(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    if (ipAddress != null && ipAddress.contains(",")) {
      ipAddress = ipAddress.split(",")[0].trim();
    }
    return ipAddress;
  }
}