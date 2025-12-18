package com.aseguradora.api.repository;

import com.aseguradora.api.model.Seguro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeguroRepository extends JpaRepository<Seguro, Long> {
    // Buscar todos los seguros de un usuario concreto
    List<Seguro> findByUsuario_IdUsuario(Long idUsuario);
    
    // Buscar por número de póliza
    boolean existsByNumPoliza(String numPoliza);
}