package com.kantus.authservice.security;

import com.kantus.authservice.util.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Clase principal de configuración de Spring Security.
 * Reducida y optimizada utilizando la Auto-Configuración de Spring Boot 3.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;

  /**
   * Configura la cadena de filtros de seguridad.
   *
   * @param http Objeto HttpSecurity para configurar la seguridad.
   * @return SecurityFilterChain configurado.
   * @throws IllegalStateException Si ocurre un error en la configuración de seguridad.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    try {
      http.csrf(AbstractHttpConfigurer::disable)
          .cors(cors -> cors.configure(http))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(SecurityConstants.getPublicMatchers()).permitAll()
              .anyRequest().authenticated())
          .sessionManagement(session -> session
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

      return http.build();
    } catch (Exception ex) {
      throw new IllegalStateException("Error crítico al inicializar la cadena de seguridad", ex);
    }
  }

  /**
   * Expone el AuthenticationManager para manejar el proceso de login.
   *
   * @param config Configuración de autenticación de Spring.
   * @return AuthenticationManager configurado.
   * @throws IllegalStateException Si ocurre un error al obtener el manager.
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
    try {
      return config.getAuthenticationManager();
    } catch (Exception ex) {
      throw new IllegalStateException("Error crítico al obtener el AuthenticationManager", ex);
    }
  }

  /**
   * Define el codificador de contraseñas utilizando BCrypt.
   *
   * @return PasswordEncoder instancia.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}