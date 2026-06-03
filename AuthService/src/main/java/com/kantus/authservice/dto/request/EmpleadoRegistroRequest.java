package com.kantus.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmpleadoRegistroRequest {

  @NotBlank(message = "El username es obligatorio")
  @Size(min = 4, max = 50, message = "El username debe tener entre 4 y 50 caracteres")
  private String username;

  @NotBlank(message = "El correo electrónico es obligatorio")
  @Email(message = "Debe ingresar un formato de correo válido")
  private String email;

  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
  private String password;

  @NotBlank(message = "El rol asignado es obligatorio")
  private String rolAsignado; // Ej: ROLE_CAJERO, ROLE_COCINERO, ROLE_MOZO
}