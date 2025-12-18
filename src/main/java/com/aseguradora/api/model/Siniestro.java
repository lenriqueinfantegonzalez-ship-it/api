package com.aseguradora.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "siniestros")
public class Siniestro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSiniestro;

    // A veces las fechas dan error 500 si no tienen formato. Esto lo arregla:
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaSuceso;

    private String descripcion;
    private String estado;
    private String resolucion;

    @ManyToOne
    @JoinColumn(name = "id_seguro")
    // ELIMINAMOS EL BUCLE INFINITO DE RAÍZ:
    // Le decimos a Java: "Cuando leas el Seguro, ignora su usuario, sus facturas, su tipo y sus siniestros".
    @JsonIgnoreProperties({"usuario", "facturas", "siniestros", "tipoSeguro", "hibernateLazyInitializer", "handler"})
    private Seguro seguro;
}