package com.aseguradora.api.controller;

import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // <--- Importante para que funcione el Map

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Herramienta de encriptación (Necesaria aquí también)
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 1. Obtener todos los usuarios
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // 2. Crear un usuario nuevo (Registro)
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        // Encriptamos la contraseña antes de guardar
        if (usuario.getPassword() != null) {
            usuario.setPassword(encoder.encode(usuario.getPassword()));
        }
        
        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(java.time.LocalDateTime.now());
        }
        
        return usuarioRepository.save(usuario);
    }
    
    // 3. Obtener un usuario por ID
    @GetMapping("/{id}")
    public Usuario obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    // 4. Actualizar Usuario (Perfil)
    @PutMapping("/{id}")
    public Usuario actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioDetalles) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            usuario.setNombreCompleto(usuarioDetalles.getNombreCompleto());
            usuario.setMovil(usuarioDetalles.getMovil());
            usuario.setDireccion(usuarioDetalles.getDireccion());
            // Nota: Aquí NO tocamos la contraseña
            return usuarioRepository.save(usuario);
        }
        return null;
    }

    // 5. CAMBIAR CONTRASEÑA (Desde Configuración) - NUEVO
    @PostMapping("/{id}/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> passwords) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) return ResponseEntity.notFound().build();

        String oldPass = passwords.get("oldPassword");
        String newPass = passwords.get("newPassword");

        // 1. Verificar que la contraseña antigua coincida
        if (!encoder.matches(oldPass, usuario.getPassword())) {
            // Si falla, probamos texto plano por si es un usuario antiguo (truco temporal)
            if (!oldPass.equals(usuario.getPassword())) {
                 return ResponseEntity.badRequest().body("La contraseña actual no es correcta.");
            }
        }

        // 2. Encriptar y guardar la nueva
        usuario.setPassword(encoder.encode(newPass));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }
}