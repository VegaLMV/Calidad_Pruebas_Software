package com.kantus.authservice.repository;
import com.kantus.authservice.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PermisoRepository extends JpaRepository<Permiso, UUID> {
  Optional<Permiso> findByCodigo(String codigo);
}