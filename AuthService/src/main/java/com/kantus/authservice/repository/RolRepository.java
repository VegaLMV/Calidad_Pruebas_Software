package com.kantus.authservice.repository;

import com.kantus.authservice.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {

  // Para buscar un rol por su nombre (Ej. "ADMIN", "CAJERO")
  Optional<Rol> findByNombre(String nombre);
}