package com.aseguradora.api.controller;

import com.aseguradora.api.model.TokenSeguridad;
import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.TokenSeguridadRepository;
import com.aseguradora.api.repository.UsuarioRepository;
import com.aseguradora.api.service.EmailService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private TokenSeguridadRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;

    // Listar todos
    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    // Obtener uno por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- EDICIÓN CON ENVÍO DE EMAIL (CORREGIDO PARA USAR STRING) ---
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @Valid @RequestBody Usuario usuarioDetalles) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        // 1. Guardamos el estado ANTERIOR
        boolean estabaActivo = usuario.getActivo();

        // 2. Actualizamos los datos
        usuario.setNombreCompleto(usuarioDetalles.getNombreCompleto());
        usuario.setRol(usuarioDetalles.getRol());
        usuario.setActivo(usuarioDetalles.getActivo());
        
        // 3. Detectamos si ha pasado de ACTIVO -> INACTIVO
        if (estabaActivo && !usuario.getActivo()) {
            
            try {
                String token = UUID.randomUUID().toString();
                
                TokenSeguridad tokenSeguridad = new TokenSeguridad();
                tokenSeguridad.setToken(token);
                tokenSeguridad.setUsuario(usuario);
                tokenSeguridad.setFechaExpiracion(LocalDateTime.now().plusHours(48)); 
                
                // --- CORRECCIÓN: Usamos String "ACTIVACION" ---
                // Tu modelo TokenSeguridad espera un String, no un Enum.
                tokenSeguridad.setTipo("ACTIVACION");
                
                tokenRepository.save(tokenSeguridad);
                
                // Intentamos enviar el correo
                emailService.enviarCorreoReactivacion(usuario.getCorreo(), token);
                System.out.println(">> Correo de reactivación enviado correctamente a: " + usuario.getCorreo());
                
            } catch (Exception e) {
                // Si falla, mostramos el error pero no rompemos la app
                System.err.println("Error crítico guardando token o enviando email: " + e.getMessage());
                e.printStackTrace(); 
            }
        }

        final Usuario updatedUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.ok(updatedUsuario);
    }

    // Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}