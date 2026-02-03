package com.aseguradora.api.controller;

import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.UsuarioRepository;
import com.aseguradora.api.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID; // <--- NECESARIO PARA GENERAR EL TOKEN

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") 
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // LISTAR TODOS
    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    // OBTENER UNO
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

  // EDITAR USUARIO (CON TOKEN DE REACTIVACIÓN)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuarioDetalles) {
        return usuarioRepository.findById(id).map(usuario -> {
            
            Boolean estadoAnterior = usuario.getActivo();

            // --- 1. VALIDACIONES (Esto ya lo tenías bien) ---
            if (usuarioDetalles.getNombreCompleto() != null && usuarioDetalles.getNombreCompleto().length() > 50) {
                return ResponseEntity.status(400).body("Error: El nombre excede los 50 caracteres.");
            }
            if (usuarioDetalles.getMovil() != null && usuarioDetalles.getMovil().length() > 9) {
                return ResponseEntity.status(400).body("Error: El móvil excede los 9 dígitos.");
            }

            // --- 2. ACTUALIZAR LOS DATOS (¡ESTO ES LO QUE FALTABA!) ---
            usuario.setRol(usuarioDetalles.getRol());

            if (usuarioDetalles.getNombreCompleto() != null) {
                usuario.setNombreCompleto(usuarioDetalles.getNombreCompleto()); // <--- AÑADIR ESTO
            }

            if (usuarioDetalles.getMovil() != null) {
                usuario.setMovil(usuarioDetalles.getMovil()); // <--- AÑADIR ESTO
            }

            // --- 3. LOGICA DE ACTIVACIÓN / DESACTIVACIÓN ---
            if (usuarioDetalles.getActivo() != null) {
                usuario.setActivo(usuarioDetalles.getActivo());
                
                // SI ESTAMOS DESACTIVANDO -> GENERAMOS TOKEN DE REACTIVACIÓN
                if (!usuario.getActivo()) {
                    usuario.setConfirmationToken(UUID.randomUUID().toString());
                }
            }

            // Guardamos
            usuarioRepository.save(usuario);

            // ENVIAR EMAIL SI HUBO CAMBIO DE ESTADO
            if (estadoAnterior != null && !estadoAnterior.equals(usuario.getActivo())) {
                
                if (usuario.getActivo()) {
                    // CASO: EL ADMIN LO REACTIVÓ MANUALMENTE
                    emailService.enviarCorreo(usuario.getCorreo(), "Cuenta Reactivada", 
                        "Hola " + usuario.getNombreCompleto() + ",\n\n" +
                        "Un administrador ha reactivado tu cuenta manualmente.\n" +
                        "Ya puedes entrar: http://127.0.0.1:5500/index.html");
                } else {
                    // CASO: SE HA DESACTIVADO -> ENVIAMOS EL LINK CON EL TOKEN
                    String linkReactivar = "http://127.0.0.1:5500/reactivar.html?token=" + usuario.getConfirmationToken();
                    
                    String mensaje = "Hola " + usuario.getNombreCompleto() + ",\n\n" +
                                     "Tu cuenta ha sido desactivada temporalmente.\n" +
                                     "Si deseas reactivarla tú mismo y volver a acceder, haz clic en el siguiente enlace:\n\n" +
                                     linkReactivar + "\n\n" +
                                     "Atentamente,\nEquipo LEIGSeguros.";
                    
                    emailService.enviarCorreo(usuario.getCorreo(), "Acción Requerida: Reactivar Cuenta", mensaje);
                }
            }

            return ResponseEntity.ok(usuario);
        }).orElse(ResponseEntity.notFound().build());
    }

    // BORRAR USUARIO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        Optional<Usuario> userOpt = usuarioRepository.findById(id);
        if (userOpt.isPresent()) {
            try {
                usuarioRepository.deleteById(id);
                return ResponseEntity.ok().body("Usuario eliminado.");
            } catch (Exception e) {
                return ResponseEntity.status(409).body("No se puede eliminar: Tiene datos asociados.");
            }
        }
        return ResponseEntity.status(404).body("Usuario no encontrado.");
    }
}