package com.kantus.authservice.exception;

import com.kantus.authservice.dto.response.MensajeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor global de excepciones. Evita que la aplicación retorne trazas de error (Stacktraces)
 * al cliente, cumpliendo con las normas de seguridad y calidad (ISO 25010).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UsuarioBloqueadoException.class)
  public ResponseEntity<MensajeResponse> handleUsuarioBloqueado(UsuarioBloqueadoException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
        new MensajeResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value())
    );
  }

  @ExceptionHandler(CredencialesInvalidasException.class)
  public ResponseEntity<MensajeResponse> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
        new MensajeResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value())
    );
  }

  /**
   * Captura los errores de validación de Jakarta (@NotBlank, @Email, etc.) en los DTOs.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidations(MethodArgumentNotValidException ex) {
    Map<String, String> errores = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String campo = ((FieldError) error).getField();
      String mensaje = error.getDefaultMessage();
      errores.put(campo, mensaje);
    });
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<MensajeResponse> handleExceptionGenerica(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
        new MensajeResponse("Ha ocurrido un error interno en el servidor", HttpStatus.INTERNAL_SERVER_ERROR.value())
    );
  }
}