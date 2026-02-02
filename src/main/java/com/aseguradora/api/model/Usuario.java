package com.aseguradora.api.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String password;

    private String rol; 
    private Boolean activo;

    // --- AQUÍ ESTABA EL OTRO ERROR ---
    @Column(name = "movil")
    private String movil; // Antes era "telefono", ahora es "movil"

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // Seguridad 2FA y Tokens
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled;
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;
    @Column(name = "confirmation_token")
    private String confirmationToken;
    @Column(name = "reset_token")
    private String resetToken;

    // Relaciones
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Seguro> seguros;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Siniestro> siniestros;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Factura> facturas;

    // CONSTRUCTORES
    public Usuario() {}

    public Usuario(String nombreCompleto, String correo, String password, String rol, Boolean activo) {
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
        this.fechaRegistro = LocalDateTime.now();
        this.twoFactorEnabled = false;
    }

    // GETTERS Y SETTERS
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    // ¡AQUÍ ESTÁ EL ARREGLO DEL ERROR setMovil!
    public String getMovil() { return movil; }
    public void setMovil(String movil) { this.movil = movil; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    
    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }

    public String getConfirmationToken() { return confirmationToken; }
    public void setConfirmationToken(String confirmationToken) { this.confirmationToken = confirmationToken; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public List<Seguro> getSeguros() { return seguros; }
    public void setSeguros(List<Seguro> seguros) { this.seguros = seguros; }

    public List<Siniestro> getSiniestros() { return siniestros; }
    public void setSiniestros(List<Siniestro> siniestros) { this.siniestros = siniestros; }

    public List<Factura> getFacturas() { return facturas; }
    public void setFacturas(List<Factura> facturas) { this.facturas = facturas; }
}