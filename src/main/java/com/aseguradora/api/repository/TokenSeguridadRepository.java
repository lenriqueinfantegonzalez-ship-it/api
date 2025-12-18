package com.aseguradora.api.repository;

import com.aseguradora.api.model.TokenSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenSeguridadRepository extends JpaRepository<TokenSeguridad, Long> {
    Optional<TokenSeguridad> findByToken(String token);
}
