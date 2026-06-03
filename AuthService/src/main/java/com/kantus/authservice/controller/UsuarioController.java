package com.kantus.authservice.controller;

import com.kantus.authservice.dto.request.EmpleadoRegistroRequest;
import com.kantus.authservice.dto.request.RegistroRequest;
import com.kantus.authservice.dto.response.MensajeResponse;
import com.kantus.authservice.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión y mantenimiento de usuarios en el sistema.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Mantenimiento de Usuarios",
    description = "Endpoints para registro y control de empleados y clientes")
public class UsuarioController {

  private final UsuarioService usuarioService;

  /**
   * Registra un nuevo usuario con rol de cliente. Endpoint público.
   *
   * @param request Datos del nuevo cliente.
   * @return ResponseEntity con confirmación de registro.
   */
  @PostMapping("/registro")
  @Operation(summary = "Registrar nuevo cliente",
      description = "Asigna automáticamente ROLE_CLIENTE. Endpoint público.")
  public ResponseEntity<MensajeResponse> registrarCliente(
      @Valid @RequestBody RegistroRequest request) {
    MensajeResponse response = usuarioService.registrarCliente(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Registra un nuevo empleado con rol específico. Requiere rol de administrador.
   *
   * @param request Datos del nuevo empleado.
   * @return ResponseEntity con confirmación de registro.
   */
  @PostMapping("/empleados")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Operation(summary = "Registrar nuevo empleado",
      description = "Permite asignar roles como ROLE_CAJERO. Requiere Token de Administrador.")
  public ResponseEntity<MensajeResponse> registrarEmpleado(
      @Valid @RequestBody EmpleadoRegistroRequest request) {
    MensajeResponse response = usuarioService.registrarEmpleado(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Cambia el estado de un usuario (Activo/Inactivo). Requiere rol de administrador.
   *
   * @param username El nombre de usuario a modificar.
   * @param body     Mapa con la clave "estado" y valor ("ACTIVO" o "INACTIVO").
   * @return ResponseEntity con confirmación de actualización.
   */
  @PutMapping("/{username}/estado")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Operation(summary = "Cambiar estado de usuario",
      description = "Activa o desactiva a un empleado. Útil para revocar accesos.")
  public ResponseEntity<MensajeResponse> cambiarEstadoUsuario(
      @PathVariable String username,
      @RequestBody Map<String, String> body) {

    String nuevoEstado = body.get("estado");
    MensajeResponse response = usuarioService.cambiarEstado(username, nuevoEstado);

    return ResponseEntity.ok(response);
  }
}