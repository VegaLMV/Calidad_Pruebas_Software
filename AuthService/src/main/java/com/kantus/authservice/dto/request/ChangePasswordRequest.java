package com.kantus.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request para cambio de contraseña del usuario autenticado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

  @NotBlank(message = "La contraseña actual es obligatoria")
  private String currentPassword;

  @NotBlank(message = "La nueva contraseña es obligatoria")
  @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
  private String newPassword;
}