package com.aseguradora.api;

import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ApiAseguradoraApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiAseguradoraApplication.class, args);
    }

    /**
     * Este método se ejecuta automáticamente al iniciar la aplicación.
     * Sirve para garantizar que SIEMPRE exista el usuario Admin con la contraseña correcta.
     */
    @Bean
    CommandLineRunner initAdmin(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            String emailAdmin = "admin@aseguradora.com";
            
            // Verificamos si ya existe el usuario
            if (usuarioRepo.existsByCorreo(emailAdmin)) {
                // Si existe, forzamos la actualización de su contraseña a "1234"
                Usuario admin = usuarioRepo.findByCorreo(emailAdmin).orElseThrow();
                admin.setPassword(passwordEncoder.encode("1234"));
                admin.setActivo(true);
                admin.setRol("ADMIN"); // Aseguramos que tenga rol ADMIN
                usuarioRepo.save(admin);
                System.out.println(">>> LOGIN REPARADO: Admin actualizado. Usuario: " + emailAdmin + " | Pass: 1234");
            } else {
                // Si no existe, lo creamos desde cero
                Usuario admin = new Usuario();
                admin.setNombreCompleto("Admin Principal");
                admin.setCorreo(emailAdmin);
                admin.setPassword(passwordEncoder.encode("1234"));
                admin.setRol("ADMIN");
                admin.setActivo(true);
                admin.setMovil("600123456");
                admin.setDireccion("Calle Principal 1");
                admin.setFechaRegistro(java.time.LocalDateTime.now());
                
                usuarioRepo.save(admin);
                System.out.println(">>> ADMIN CREADO: Usuario: " + emailAdmin + " | Pass: 1234");
            }
        };
    }
}
