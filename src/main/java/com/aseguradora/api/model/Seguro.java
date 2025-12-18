package com.aseguradora.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "seguros")
public class Seguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguro")
    private Long idSeguro;

    @Column(name = "num_poliza", nullable = false, unique = true, length = 20)
    private String numPoliza;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_renovacion", nullable = false)
    private LocalDate fechaRenovacion;

    @Column(name = "prima_anual", nullable = false)
    private BigDecimal primaAnual;

    @Column(name = "datos_especificos", columnDefinition = "TEXT")
    private String datosEspecificos;

    @Column(length = 20)
    private String estado;

    // Relaciones (Foreign Keys)
    
    @ManyToOne // Muchos seguros pueden ser de un usuario
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne // Muchos seguros pueden ser de un tipo (ej. Coche)
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoSeguro tipoSeguro;
}
