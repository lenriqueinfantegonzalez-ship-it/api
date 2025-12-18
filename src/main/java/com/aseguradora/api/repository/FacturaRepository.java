package com.aseguradora.api.repository;

import com.aseguradora.api.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    // Ver facturas de un usuario
    List<Factura> findByUsuario_IdUsuario(Long idUsuario);
}