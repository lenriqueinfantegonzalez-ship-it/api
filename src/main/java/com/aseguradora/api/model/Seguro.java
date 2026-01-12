package com.aseguradora.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <--- AÑADIR IMPORT
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

    // --- RELACIONES CORREGIDAS ---

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    // ESTA LÍNEA ES VITAL: Evita que al pedir seguros se rompa intentando leer el usuario entero
    @JsonIgnoreProperties({"password", "roles", "hibernateLazyInitializer", "handler"}) 
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoSeguro tipoSeguro;
}