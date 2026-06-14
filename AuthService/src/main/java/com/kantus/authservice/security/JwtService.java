package com.kantus.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la generación, validación y extracción de claims de los JSON Web Tokens.
 */
@Service
public class JwtService {

  /**
   * Llave secreta inyectada desde application.properties.
   */
  @Value("${application.security.jwt.secret-key}")
  private String secretKey;

  /**
   * Tiempo de expiración del token JWT.
   */
  @Value("${application.security.jwt.expiration:86400000}")
  private long jwtExpiration;

  /**
   * Extrae el subject del token. En este proyecto el subject representa el UUID del usuario.
   *
   * @param token Token JWT.
   * @return UUID del usuario en formato texto.
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Genera un token sin claims adicionales.
   *
   * @param userDetails Detalles del usuario.
   * @return JWT generado.
   */
  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  /**
   * Genera un token con claims adicionales.
   *
   * @param extraClaims Claims adicionales como username, roles y permissions.
   * @param userDetails Detalles del usuario.
   * @return JWT generado.
   */
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(getSignInKey(), Jwts.SIG.HS256)
        .compact();
  }

  /**
   * Valida si un token pertenece al usuario y no está expirado.
   *
   * @param token Token JWT.
   * @param userDetails Detalles del usuario.
   * @return true si el token es válido.
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  /**
   * Extrae el username real almacenado como claim.
   *
   * @param token Token JWT.
   * @return Username del usuario.
   */
  public String extractRealUsername(String token) {
    return extractClaim(token, claims -> claims.get("username", String.class));
  }

  /**
   * Extrae los roles del token.
   *
   * @param token Token JWT.
   * @return Lista de roles.
   */
  @SuppressWarnings("unchecked")
  public List<String> extractRoles(String token) {
    return extractClaim(token, claims -> {
      Object roles = claims.get("roles");
      if (roles instanceof List<?>) {
        return (List<String>) roles;
      }
      return Collections.emptyList();
    });
  }

  /**
   * Extrae los permisos del token.
   *
   * @param token Token JWT.
   * @return Lista de permisos.
   */
  @SuppressWarnings("unchecked")
  public List<String> extractPermissions(String token) {
    return extractClaim(token, claims -> {
      Object permissions = claims.get("permissions");
      if (permissions instanceof List<?>) {
        return (List<String>) permissions;
      }
      return Collections.emptyList();
    });
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(final String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(final String token) {
    return Jwts.parser()
        .verifyWith(getSignInKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}