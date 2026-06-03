package com.kantus.authservice.config;

import com.kantus.authservice.entity.Permiso;
import com.kantus.authservice.entity.Rol;
import com.kantus.authservice.entity.RolPermiso;
import com.kantus.authservice.entity.Usuario;
import com.kantus.authservice.entity.UsuarioRol;
import com.kantus.authservice.repository.PermisoRepository;
import com.kantus.authservice.repository.RolPermisoRepository;
import com.kantus.authservice.repository.RolRepository;
import com.kantus.authservice.repository.UsuarioRepository;
import com.kantus.authservice.repository.UsuarioRolRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Componente para inicializar datos maestros y configuraciones iniciales del sistema.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final RolRepository rolRepository;
  private final PermisoRepository permisoRepository;
  private final RolPermisoRepository rolPermisoRepository;
  private final UsuarioRepository usuarioRepository;
  private final UsuarioRolRepository usuarioRolRepository;
  private final PasswordEncoder passwordEncoder;

  // 1. Inyectamos la variable desde el archivo de configuración properties
  @Value("${kantus.admin.default-password}")
  private String adminDefaultPassword;

  @Override
  @Transactional
  public void run(String... args) {
    // 1. Crear Roles
    Rol rolAdmin = crearRol("ROLE_ADMIN", "Administrador principal", true);
    crearRol("ROLE_CLIENTE", "Cliente", false);

    // 2. Crear Permisos y 3. Vincular Permisos a Roles
    Permiso crearPlato = crearPermiso("MENU_CREAR_PLATO",
        "Permite crear nuevos platos", "CATALOGO");
    asignarPermisoRol(rolAdmin, crearPlato);

    Permiso cobrarMesa = crearPermiso("CAJA_COBRAR_MESA",
        "Permite cerrar cuentas y cobrar", "CAJA");
    asignarPermisoRol(rolAdmin, cobrarMesa);

    Rol rolCajero = crearRol("ROLE_CAJERO", "Cajero", true);
    asignarPermisoRol(rolCajero, cobrarMesa);

    Permiso tomarPedido = crearPermiso("PEDIDO_TOMAR",
        "Permite registrar pedido", "OPERACIONES");
    Rol rolMozo = crearRol("ROLE_MOZO", "Mozo", true);
    asignarPermisoRol(rolMozo, tomarPedido);

    // 4. Crear Super Administrador
    crearAdminMaestro(rolAdmin);
  }

  private Rol crearRol(String nombre, String descripcion, boolean esSistema) {
    return rolRepository.findByNombre(nombre).orElseGet(() ->
        rolRepository.save(Rol.builder()
            .nombre(nombre)
            .descripcion(descripcion)
            .esSistema(esSistema)
            .build()));
  }

  private Permiso crearPermiso(String codigo, String descripcion, String modulo) {
    return permisoRepository.findByCodigo(codigo).orElseGet(() ->
        permisoRepository.save(Permiso.builder()
            .codigo(codigo)
            .descripcion(descripcion)
            .modulo(modulo)
            .build()));
  }

  // Renombramos el método de 'asignarPermisoARol' a 'asignarPermisoRol'
  private void asignarPermisoRol(Rol rol, Permiso permiso) {
    if (!rolPermisoRepository.existsByRolAndPermiso(rol, permiso)) {
      RolPermiso rolPermiso = RolPermiso.builder()
          .rol(rol)
          .permiso(permiso)
          .build();

      rolPermisoRepository.save(rolPermiso);
    }
  }

  private void crearAdminMaestro(Rol rolAdmin) {
    if (usuarioRepository.findByUsername("superadmin").isEmpty()) {
      Usuario admin = usuarioRepository.save(Usuario.builder()
          .username("superadmin")
          .email("admin@kantus.com")
          // 2. Usamos la variable inyectada en lugar de la contraseña quemada
          .passwordHash(passwordEncoder.encode(adminDefaultPassword))
          .intentosFallidos((short) 0)
          .mfaHabilitado(false)
          .requiereCambioPassword(false)
          .build());

      usuarioRolRepository.save(UsuarioRol.builder()
          .usuario(admin)
          .rol(rolAdmin)
          .fechaAsignacion(LocalDateTime.now())
          .build());
    }
  }
}