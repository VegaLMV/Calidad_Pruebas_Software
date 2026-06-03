package com.kantus.authservice.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
  @NotBlank(message = "El Refresh Token es obligatorio")
  private String refreshToken;
}