package com.aseguradora.api.controller;

import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.UsuarioRepository;
import com.aseguradora.api.security.JwtUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // <--- LOGS PROFESIONALES
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // <--- VALIDACIÓN DE DATOS

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Inicializamos el sistema de Logs
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Instancia para 2FA
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // --- REGISTRO (Ahora con @Valid para rechazar emails falsos) ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Usuario nuevoUsuario) {
        try {
            if (usuarioRepository.findByCorreo(nuevoUsuario.getCorreo()).isPresent()) {
                logger.warn("Intento de registro fallido: El correo {} ya existe", nuevoUsuario.getCorreo());
                return ResponseEntity.status(400).body("El correo ya está registrado.");
            }

            // Encriptamos contraseña
            nuevoUsuario.setPassword(passwordEncoder.encode(nuevoUsuario.getPassword()));
            
            // Configuramos rol y estado por defecto
            if (nuevoUsuario.getRol() == null) nuevoUsuario.setRol("USER");
            nuevoUsuario.setActivo(true);
            nuevoUsuario.setFechaRegistro(java.time.LocalDateTime.now());
            
            // 2FA Desactivado al inicio
            nuevoUsuario.setTwoFactorEnabled(false); 
            nuevoUsuario.setTwoFactorSecret(null);

            usuarioRepository.save(nuevoUsuario);
            
            logger.info("Nuevo usuario registrado con éxito: {}", nuevoUsuario.getCorreo());
            return ResponseEntity.ok("Usuario registrado con éxito");

        } catch (Exception e) {
            logger.error("Error crítico en el registro: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error en el servidor al registrar.");
        }
    }

   @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String correo = credenciales.get("correo");
        String password = credenciales.get("password");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // 1. Verificar contraseña
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                
                // 2. Verificar si está ACTIVO
                if (!usuario.getActivo()) {
                    return ResponseEntity.status(403).body("Tu cuenta está desactivada.");
                }

                // 3. BLOQUEO 2FA (Aquí está la clave)
                // Si el usuario tiene el 2FA activado, NO le damos el token todavía.
                if (Boolean.TRUE.equals(usuario.getTwoFactorEnabled())) {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "2FA_REQUIRED"); // Esta es la señal que espera tu HTML
                    response.put("correo", correo);
                    return ResponseEntity.ok(response);
                }

                // 4. Si NO tiene 2FA, generamos el Token y entra normal
                String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getRol(), usuario.getIdUsuario());
                
                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("token", token);
                respuesta.put("usuario", usuario);
                
                return ResponseEntity.ok(respuesta);
            }
        }
        
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }

    // --- SETUP 2FA (Generar QR) ---
    @PostMapping("/setup-2fa")
    public ResponseEntity<?> setup2fa(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar clave secreta
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();
        
        // Guardar secreto temporalmente (o definitivo según lógica)
        usuario.setTwoFactorSecret(secret);
        usuarioRepository.save(usuario);

        // Generar URL para código QR (formato compatible con Google Authenticator)
        String qrUrl = String.format("otpauth://totp/AseguradoraApp:%s?secret=%s&issuer=AseguradoraApp", correo, secret);
        
        Map<String, String> response = new HashMap<>();
        response.put("qrUrl", qrUrl);
        response.put("secret", secret);
        
        logger.info("Iniciado proceso de activación 2FA para: {}", correo);
        return ResponseEntity.ok(response);
    }

    // --- CONFIRMAR 2FA ---
    @PostMapping("/confirm-2fa")
    public ResponseEntity<?> confirm2fa(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String codigoStr = body.get("codigo"); // El código que introduce el usuario
        int codigo = Integer.parseInt(codigoStr);

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si el código coincide con el secreto guardado
        boolean isCodeValid = gAuth.authorize(usuario.getTwoFactorSecret(), codigo);

        if (isCodeValid) {
            usuario.setTwoFactorEnabled(true);
            usuarioRepository.save(usuario);
            logger.info("2FA activado correctamente para: {}", correo);
            return ResponseEntity.ok("2FA Activado correctamente");
        } else {
            logger.warn("Fallo al confirmar 2FA para {}: Código incorrecto", correo);
            return ResponseEntity.status(400).body("Código incorrecto");
        }
    }
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2faLogin(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String codigoStr = body.get("codigo");
        
        try {
            int codigo = Integer.parseInt(codigoStr);
            Usuario usuario = usuarioRepository.findByCorreo(correo)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Validamos con Google Authenticator
            boolean isCodeValid = gAuth.authorize(usuario.getTwoFactorSecret(), codigo);

            if (isCodeValid) {
                // Código correcto -> AHORA SÍ generamos el token
                String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getRol(), usuario.getIdUsuario());
                
                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("token", token);
                respuesta.put("usuario", usuario);
                
                return ResponseEntity.ok(respuesta);
            } else {
                return ResponseEntity.status(401).body("Código incorrecto");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error verificando código");
        }
    }
    
    // --- DESACTIVAR 2FA ---
    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2fa(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setTwoFactorEnabled(false);
        usuario.setTwoFactorSecret(null);
        usuarioRepository.save(usuario);
        
        logger.info("2FA desactivado para: {}", correo);
        return ResponseEntity.ok("2FA Desactivado");
    }
}