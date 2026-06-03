package com.kantus.authservice.repository;
import com.kantus.authservice.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, UUID> {
  // Para validar si el token de refresco existe y es válido
  Optional<Sesion> findByRefreshTokenHash(String refreshTokenHash);
}