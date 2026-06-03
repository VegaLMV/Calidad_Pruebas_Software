package com.kantus.authservice.repository;

import com.kantus.authservice.entity.AuthAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, UUID> {
  // Aquí se guardará el historial de todo. Por ahora, los métodos por defecto de JpaRepository (save) son suficientes.
}