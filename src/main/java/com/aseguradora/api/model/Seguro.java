package com.aseguradora.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; // <--- IMPORT NUEVO
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

    @NotBlank(message = "El número de póliza es obligatorio")
    @Column(name = "num_poliza", nullable = false, unique = true, length = 20)
    private String numPoliza;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de renovación es obligatoria")
    @Column(name = "fecha_renovacion", nullable = false)
    private LocalDate fechaRenovacion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que 0") // <--- ¡AQUÍ ESTÁ LA MAGIA! Evita negativos
    @Column(name = "prima_anual", nullable = false)
    private BigDecimal primaAnual;

    @Column(name = "datos_especificos", columnDefinition = "TEXT")
    private String datosEspecificos;

    @Column(length = 20)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"password", "roles", "hibernateLazyInitializer", "handler"}) 
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoSeguro tipoSeguro;
}