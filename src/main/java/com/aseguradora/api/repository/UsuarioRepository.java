package com.aseguradora.api.repository;

import com.aseguradora.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Buscar por correo (para Login y Registro)
    Optional<Usuario> findByCorreo(String correo);
    
    // Verificar si existe (para evitar duplicados)
    boolean existsByCorreo(String correo);

    // --- ESTOS SON LOS QUE FALTABAN PARA LOS EMAILS ---
    
    // Buscar por Token de Confirmación (Registro)
    Optional<Usuario> findByConfirmationToken(String token);
    
    // Buscar por Token de Recuperación (Olvidé Contraseña)
    Optional<Usuario> findByResetToken(String token);
}