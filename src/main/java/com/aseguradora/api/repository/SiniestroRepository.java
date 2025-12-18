package com.aseguradora.api.repository;

import com.aseguradora.api.model.Siniestro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SiniestroRepository extends JpaRepository<Siniestro, Long> {
    // Buscar siniestros de un seguro concreto
    List<Siniestro> findBySeguro_IdSeguro(Long idSeguro);
    
    // Buscar siniestros de un usuario (a través del seguro)
    List<Siniestro> findBySeguro_Usuario_IdUsuario(Long idUsuario);
}
