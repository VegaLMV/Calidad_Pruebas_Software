package com.kantus.authservice.repository;

import com.kantus.authservice.entity.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UUID> {

  // Método clave para traer todos los roles de un usuario específico
  List<UsuarioRol> findByUsuarioId(UUID usuarioId);
}