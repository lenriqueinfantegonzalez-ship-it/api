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
}