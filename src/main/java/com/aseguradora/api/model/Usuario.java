package com.aseguradora.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Lombok: Crea automáticamente getters, setters y toString
@Entity // Esto le dice a Java: "Esta clase representa una tabla en la BD"
@Table(name = "usuarios") // Nombre exacto de la tabla en SQL
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre_completo", nullable = false, length = 50)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false)
    private String password;

    @Column(length = 15)
    private String movil;

    private String direccion;

    @Column(length = 20)
    private String rol; // 'ADMIN' o 'USER'

    private Boolean activo;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
   // ... otros campos ...

    // --- CAMPOS NUEVOS PARA GOOGLE AUTHENTICATOR ---
    
    @Column(name = "two_factor_secret")
    private String twoFactorSecret; // Aquí guardamos la clave Base32
    
    // Requisito: Límite de intentos
    @Column(name = "intentos_2fa")
    private Integer intentos2fa = 0; 
    
    // NOTA: Borra twoFactorCode y twoFactorExpiry, ya no sirven.

    // Opcional: Para saber si este usuario tiene obligado el 2FA o no.
    // Lo pondremos a 'true' por defecto o según prefieras.
    @Column(name = "two_factor_enabled") 
    private Boolean twoFactorEnabled = true; 
}
