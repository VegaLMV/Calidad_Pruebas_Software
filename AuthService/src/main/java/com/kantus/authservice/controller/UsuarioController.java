package com.kantus.authservice.controller;

import com.kantus.authservice.dto.request.EmpleadoRegistroRequest;
import com.kantus.authservice.dto.request.RegistroRequest;
import com.kantus.authservice.dto.response.MensajeResponse;
import com.kantus.authservice.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Mantenimiento de Usuarios", description = "Endpoints para registro y control de empleados y clientes")
public class UsuarioController {

  private final UsuarioService usuarioService;

  /**
   * Endpoint PÚBLICO para clientes.
   */
  @PostMapping("/registro")
  @Operation(summary = "Registrar nuevo cliente", description = "Asigna automáticamente ROLE_CLIENTE. Endpoint público.")
  public ResponseEntity<MensajeResponse> registrarCliente(@Valid @RequestBody RegistroRequest request) {
    MensajeResponse response = usuarioService.registrarCliente(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Endpoint PRIVADO para empleados. Requiere JWT con rol ADMIN.
   */
  @PostMapping("/empleados")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Operation(summary = "Registrar nuevo empleado", description = "Permite asignar roles como ROLE_CAJERO. Requiere Token de Administrador.")
  public ResponseEntity<MensajeResponse> registrarEmpleado(@Valid @RequestBody EmpleadoRegistroRequest request) {
    MensajeResponse response = usuarioService.registrarEmpleado(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}