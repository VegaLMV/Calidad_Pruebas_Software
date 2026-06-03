package com.kantus.authservice.repository;

import com.kantus.authservice.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

  // Método crítico para el login: buscar al usuario por su username
  Optional<Usuario> findByUsername(String username);

  // Validaciones para no duplicar datos al registrar
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
}