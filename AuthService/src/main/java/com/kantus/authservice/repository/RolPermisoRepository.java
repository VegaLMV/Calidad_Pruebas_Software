package com.kantus.authservice.repository;
import com.kantus.authservice.entity.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, UUID> {}