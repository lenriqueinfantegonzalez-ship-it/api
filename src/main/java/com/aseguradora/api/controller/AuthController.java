package com.aseguradora.api.controller;

import com.aseguradora.api.dto.LoginRequest;
import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Herramienta de encriptación
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElse(null);

        // Validamos si existe el usuario
        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }

        // COMPROBACIÓN SEGURA:
        // encoder.matches(contraseña_escrita, contraseña_encriptada_en_bd)
        // NOTA: Si la contraseña en BD es "plana" (antigua), matches fallará. 
        // Para arreglarlo, usa el Reset Password después de reiniciar.
        boolean coincide = encoder.matches(request.getPassword(), usuario.getPassword());
        
        // *Truco temporal*: Si falla la encriptada, probamos texto plano 
        // (para que no se te bloquee el admin antiguo mientras migras)
        if (!coincide && request.getPassword().equals(usuario.getPassword())) {
            coincide = true; 
        }

        if (!coincide) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }

        if (!usuario.getActivo()) {
            return ResponseEntity.status(401).body("Cuenta inactiva");
        }

        return ResponseEntity.ok(usuario);
    }

    // RESET PASSWORD (Ahora guarda encriptado)
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String correo = payload.get("correo");
        String nuevaPassword = payload.get("nuevaPassword");

        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

        if (usuario == null) {
            return ResponseEntity.badRequest().body("Error: Ese correo no existe.");
        }

        // ENCRIPTAMOS ANTES DE GUARDAR
        usuario.setPassword(encoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok().body("Contraseña actualizada y encriptada.");
    }
}