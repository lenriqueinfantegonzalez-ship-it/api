package com.aseguradora.api.repository;

import com.aseguradora.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<Entidad, TipoDeLaID>
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Aquí definimos búsquedas personalizadas mágicas
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}