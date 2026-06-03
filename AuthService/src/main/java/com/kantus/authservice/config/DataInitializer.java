package com.kantus.authservice.config;

import com.kantus.authservice.entity.*;
import com.kantus.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final RolRepository rolRepository;
  private final PermisoRepository permisoRepository;
  private final RolPermisoRepository rolPermisoRepository;
  private final UsuarioRepository usuarioRepository;
  private final UsuarioRolRepository usuarioRolRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    // 1. Crear Roles
    Rol rolAdmin = crearRol("ROLE_ADMIN", "Administrador principal", true);
    Rol rolCajero = crearRol("ROLE_CAJERO", "Cajero", true);
    Rol rolMozo = crearRol("ROLE_MOZO", "Mozo", true);
    Rol rolCliente = crearRol("ROLE_CLIENTE", "Cliente", false);

    // 2. Crear Permisos (Módulo Menú y Usuarios)
    Permiso crearPlato = crearPermiso("MENU_CREAR_PLATO", "Permite crear nuevos platos", "CATALOGO");
    Permiso cobrarMesa = crearPermiso("CAJA_COBRAR_MESA", "Permite cerrar cuentas y cobrar", "CAJA");
    Permiso tomarPedido = crearPermiso("PEDIDO_TOMAR", "Permite registrar un pedido de cliente", "OPERACIONES");

    // 3. Vincular Permisos a Roles (Matriz de Seguridad)
    asignarPermisoARol(rolAdmin, crearPlato);
    asignarPermisoARol(rolAdmin, cobrarMesa);
    asignarPermisoARol(rolCajero, cobrarMesa);
    asignarPermisoARol(rolMozo, tomarPedido);

    // 4. Crear Super Administrador
    crearAdminMaestro(rolAdmin);
  }

  private Rol crearRol(String nombre, String descripcion, boolean esSistema) {
    return rolRepository.findByNombre(nombre).orElseGet(() ->
        rolRepository.save(Rol.builder().nombre(nombre).descripcion(descripcion).esSistema(esSistema).build())
    );
  }

  private Permiso crearPermiso(String codigo, String descripcion, String modulo) {
    return permisoRepository.findByCodigo(codigo).orElseGet(() ->
        permisoRepository.save(Permiso.builder().codigo(codigo).descripcion(descripcion).modulo(modulo).build())
    );
  }

  private void asignarPermisoARol(Rol rol, Permiso permiso) {
    // Validación simple para no duplicar (en un proyecto real se busca si ya existe la relación)
    rolPermisoRepository.save(RolPermiso.builder().rol(rol).permiso(permiso).build());
  }

  private void crearAdminMaestro(Rol rolAdmin) {
    if (usuarioRepository.findByUsername("superadmin").isEmpty()) {
      Usuario admin = usuarioRepository.save(Usuario.builder()
          .username("superadmin")
          .email("admin@kantus.com")
          .passwordHash(passwordEncoder.encode("KantusAdmin123!"))
          .intentosFallidos((short) 0)
          .mfaHabilitado(false)
          .requiereCambioPassword(false)
          .build());

      usuarioRolRepository.save(UsuarioRol.builder()
          .usuario(admin).rol(rolAdmin).fechaAsignacion(LocalDateTime.now()).build());
    }
  }
}