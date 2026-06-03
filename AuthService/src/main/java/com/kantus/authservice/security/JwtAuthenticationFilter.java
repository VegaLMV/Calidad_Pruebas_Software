package com.kantus.authservice.security;

import com.kantus.authservice.util.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que intercepta cada petición HTTP para validar la presencia y validez del Token JWT.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {

    final String authHeader = request.getHeader(SecurityConstants.AUTH_HEADER);
    final String jwt;
    final String userUuid;

    // 1. Validar si existe el header y empieza con "Bearer "
    if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    // 2. Extraer el token puro
    jwt = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());
    userUuid = jwtService.extractUsername(jwt);

    // 3. Si hay un UUID y no hay una sesión activa en este hilo
    if (userUuid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(userUuid);

      // 4. Si el token es válido, inyectamos la autenticación en Spring Security
      if (jwtService.isTokenValid(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Actualizamos el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    // Continuar con la cadena de filtros
    filterChain.doFilter(request, response);
  }
}