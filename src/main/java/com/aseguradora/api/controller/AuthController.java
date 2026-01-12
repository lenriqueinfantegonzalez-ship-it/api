package com.aseguradora.api.controller;

import com.aseguradora.api.dto.LoginRequest;
import com.aseguradora.api.model.TokenSeguridad;
import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.TokenSeguridadRepository;
import com.aseguradora.api.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // Necesario ahora
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository; // Importante
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenSeguridadRepository tokenRepository;

    @Autowired
    private PasswordEncoder encoder;

    // INYECTAMOS EL REPOSITORIO (Esto es lo nuevo y vital)
    @Autowired
    private SecurityContextRepository securityContextRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. Autenticar
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getCorreo(),
                            loginRequest.getPassword()
                    )
            );

            // 2. Crear Contexto
            SecurityContext sc = SecurityContextHolder.createEmptyContext();
            sc.setAuthentication(authentication);
            SecurityContextHolder.setContext(sc);

            // 3. GUARDADO OFICIAL (La solución al 403)
            // Esto guarda la sesión y escribe la cookie JSESSIONID en la respuesta automáticamente
            securityContextRepository.saveContext(sc, request, response);

            // 4. Devolver respuesta
            Usuario usuario = usuarioRepository.findByCorreo(loginRequest.getCorreo()).orElseThrow();
            
            Map<String, Object> resp = new HashMap<>();
            resp.put("mensaje", "Login exitoso");
            resp.put("usuario", usuario);
            resp.put("rol", usuario.getRol());

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
    }

    // --- MÉTODOS DE RECUPERACIÓN (Sin cambios) ---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        // ... (Tu código existente o el que te pasé antes) ...
        // Para resumir aquí pongo el return simple, pero mantén tu lógica de tokens
        return ResponseEntity.ok("Si existe, enviado.");
    }
    
    @PostMapping("/reset-password-token")
    public ResponseEntity<?> resetPasswordToken(@RequestBody Map<String, String> payload) {
        // ... (Tu código existente) ...
        return ResponseEntity.ok("Contraseña cambiada");
    }
}