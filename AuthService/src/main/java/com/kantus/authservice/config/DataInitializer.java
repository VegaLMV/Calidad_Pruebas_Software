package com.kantus.authservice.config;

import com.kantus.authservice.entity.Rol;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.entity.UsuarioRol;
import com.kantus.authservice.repository.RolRepository;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.repository.UsuarioRolRepository;
import com.kantus.authservice.util.DateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializa únicamente el usuario administrador maestro del sistema.
 *
 * <p>Los roles, permisos y relaciones rol-permiso se gestionan mediante Flyway
 * en V3__seed_roles_permissions.sql. Este componente no debe crear permisos
 * para evitar duplicidad o inconsistencias con las migraciones.</p>
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private static final String SUPERADMIN_USERNAME = "superadmin";
  private static final String SUPERADMIN_EMAIL = "admin@kantus.com";
  private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

  private final RolRepository rolRepository;
  private final UsuarioRepository usuarioRepository;
  private final UsuarioRolRepository usuarioRolRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${kantus.admin.default-password}")
  private String adminDefaultPassword;

  @Override
  @Transactional
  public void run(String... args) {
    Rol rolAdmin = obtenerRolAdmin();
    Usuario superadmin = obtenerSuperadmin();

    asignarRolAdminSiNoExiste(superadmin, rolAdmin);
  }

  private Rol obtenerRolAdmin() {
    return rolRepository.findByNombre(ADMIN_ROLE_NAME)
        .orElseThrow(() -> new IllegalStateException(
            "No existe el rol ROLE_ADMIN. Verifica que Flyway haya ejecutado "
                + "correctamente V3__seed_roles_permissions.sql."));
  }

  private Usuario obtenerSuperadmin() {
    return usuarioRepository.findByUsername(SUPERADMIN_USERNAME)
        .orElseGet(this::crearSuperadmin);
  }

  private Usuario crearSuperadmin() {
    Usuario admin = Usuario.builder()
        .username(SUPERADMIN_USERNAME)
        .email(SUPERADMIN_EMAIL)
        .passwordHash(passwordEncoder.encode(adminDefaultPassword))
        .requiereCambioPassword(false)
        .mfaHabilitado(false)
        .intentosFallidos((short) 0)
        .build();

    return usuarioRepository.save(admin);
  }

  private void asignarRolAdminSiNoExiste(Usuario usuario, Rol rolAdmin) {
    boolean yaTieneRolAdmin = usuarioRolRepository.existsByUsuarioIdAndRolId(
        usuario.getId(),
        rolAdmin.getId()
    );

    if (!yaTieneRolAdmin) {
      UsuarioRol usuarioRol = UsuarioRol.builder()
          .usuario(usuario)
          .rol(rolAdmin)
          .fechaAsignacion(DateTimeProvider.now())
          .build();

      usuarioRolRepository.save(usuarioRol);
    }
  }
}