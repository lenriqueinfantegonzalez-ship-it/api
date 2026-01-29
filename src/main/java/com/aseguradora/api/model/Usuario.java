package com.aseguradora.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // <--- NUEVO IMPORT VITAL
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @NotBlank(message = "El nombre no puede estar vacío") // <--- VALIDACIÓN
    @Column(name = "nombre_completo", nullable = false, length = 50)
    private String nombreCompleto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido") // <--- VALIDA QUE TENGA @ Y PUNTO
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false)
    private String password;

    @Column(length = 15)
    private String movil;

    private String direccion;

    @Column(length = 20)
    private String rol;

    private Boolean activo;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // --- 2FA ---
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;
    
    @Column(name = "intentos_2fa")
    private Integer intentos2fa = 0;

    @Column(name = "two_factor_enabled") 
    private Boolean twoFactorEnabled = true; 
}