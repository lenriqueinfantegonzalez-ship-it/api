package com.aseguradora.api.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@Entity
@Table(name = "siniestros")
public class Siniestro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_siniestro")
    private Long idSiniestro;

    // --- CAMBIO CLAVE: Renombrado a fecha_suceso ---
    @Column(name = "fecha_suceso", nullable = false)
    private LocalDate fechaSuceso; 

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String estado; // "ABIERTO", "EN PROCESO", "CERRADO"

    private String resolucion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties("siniestros")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_seguro")
    @JsonIgnoreProperties("siniestros")
    private Seguro seguro;

    // ==========================================
    // GETTERS Y SETTERS CORREGIDOS
    // ==========================================

    public Long getIdSiniestro() { return idSiniestro; }
    public void setIdSiniestro(Long idSiniestro) { this.idSiniestro = idSiniestro; }

    // ¡ESTE ES EL MÉTODO QUE TE FALTABA!
    public LocalDate getFechaSuceso() { return fechaSuceso; }
    public void setFechaSuceso(LocalDate fechaSuceso) { this.fechaSuceso = fechaSuceso; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Seguro getSeguro() { return seguro; }
    public void setSeguro(Seguro seguro) { this.seguro = seguro; }
}