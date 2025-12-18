package com.aseguradora.api.repository;

import com.aseguradora.api.model.TipoSeguro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoSeguroRepository extends JpaRepository<TipoSeguro, Long> {
    // Nada especial por ahora, con los métodos básicos nos vale
}