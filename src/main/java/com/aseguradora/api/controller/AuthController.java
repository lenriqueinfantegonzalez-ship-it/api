package com.aseguradora.api.controller;

import com.aseguradora.api.dto.LoginRequest;
import com.aseguradora.api.model.TokenSeguridad;
import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.TokenSeguridadRepository;
import com.aseguradora.api.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @Autowired
    private SecurityContextRepository securityContextRepository;

    // --- 1. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getCorreo(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContext sc = SecurityContextHolder.createEmptyContext();
            sc.setAuthentication(authentication);
            SecurityContextHolder.setContext(sc);

            securityContextRepository.saveContext(sc, request, response);

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

    // --- 2. REGISTRO (¡NUEVO! - Esto arregla el error 404) ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario nuevoUsuario) {
        // Verificar si el correo ya existe
        if (usuarioRepository.findByCorreo(nuevoUsuario.getCorreo()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: El correo ya está registrado.");
        }

        // Crear usuario
        Usuario user = new Usuario();
        user.setNombreCompleto(nuevoUsuario.getNombreCompleto());
        user.setCorreo(nuevoUsuario.getCorreo());
        // IMPORTANTE: Cifrar la contraseña
        user.setPassword(encoder.encode(nuevoUsuario.getPassword()));
        user.setRol(nuevoUsuario.getRol()); // USER o ADMIN
        user.setActivo(true);
        user.setFechaRegistro(LocalDateTime.now());
        
        // Datos opcionales por defecto
        user.setMovil(nuevoUsuario.getMovil() != null ? nuevoUsuario.getMovil() : "");
        user.setDireccion("");

        usuarioRepository.save(user);

        return ResponseEntity.ok("Usuario registrado exitosamente.");
    }

    // --- 3. RECUPERACIÓN DE CONTRASEÑA ---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String correo = payload.get("correo");
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        
        if (usuario != null) {
            String tokenStr = java.util.UUID.randomUUID().toString();
            TokenSeguridad token = new TokenSeguridad();
            token.setToken(tokenStr);
            token.setUsuario(usuario);
            token.setTipo("RECUPERACION");
            token.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
            tokenRepository.save(token);
            
            System.out.println(">>> EMAIL SIMULADO: Token para " + correo + ": " + tokenStr);
        }
        return ResponseEntity.ok("Si el correo existe, se ha enviado un enlace.");
    }

    @PostMapping("/reset-password-token")
    public ResponseEntity<?> resetPasswordToken(@RequestBody Map<String, String> payload) {
        String tokenStr = payload.get("token");
        String nuevaPassword = payload.get("nuevaPassword");

        TokenSeguridad tokenDB = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (tokenDB.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("Token expirado");
        }

        Usuario usuario = tokenDB.getUsuario();
        usuario.setPassword(encoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        tokenRepository.delete(tokenDB);

        return ResponseEntity.ok("Contraseña actualizada.");
    }
}