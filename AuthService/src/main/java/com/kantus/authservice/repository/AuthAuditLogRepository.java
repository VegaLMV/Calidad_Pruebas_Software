package com.kantus.authservice.repository;

import com.kantus.authservice.entity.AuthAuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de logs de auditoría.
 */
@Repository
public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, UUID> {
  // Historial de eventos de seguridad.
  // Los métodos de JpaRepository son suficientes para esta entidad.
}