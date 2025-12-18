package com.aseguradora.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tipos_seguro")
public class TipoSeguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Long idTipo;

    @Column(nullable = false, length = 50)
    private String nombre; // Coche, Hogar, etc.

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_base")
    private BigDecimal precioBase;
}
