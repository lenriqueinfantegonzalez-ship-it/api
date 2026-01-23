package com.aseguradora.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString; // <--- NUEVO IMPORT IMPORTANTE

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;

    @Column(name = "fecha_emision", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd") // Formato obligatorio para que no falle
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private BigDecimal importe;

    @Column(length = 100)
    private String concepto;

    @Column(length = 20)
    private String estado;

    // --- RELACIONES BLINDADAS ---

    @ManyToOne
    @JoinColumn(name = "id_seguro", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "usuario"}) // Ignoramos usuario para no repetir
    @ToString.Exclude // <--- EVITA EL BUCLE DE MEMORIA AL IMPRIMIR LOGS
    private Seguro seguro;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "rol", "roles"})
    @ToString.Exclude // <--- EVITA EL BUCLE DE MEMORIA AL IMPRIMIR LOGS
    private Usuario usuario;
}