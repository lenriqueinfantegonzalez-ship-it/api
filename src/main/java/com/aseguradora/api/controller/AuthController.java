package com.aseguradora.api.controller;

import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.UsuarioRepository;
import com.aseguradora.api.security.JwtUtil;
import com.aseguradora.api.service.EmailService; // IMPORTANTE: Tu nuevo servicio de email
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // INYECCIÓN DEL SERVICIO DE EMAIL REAL
    @Autowired
    private EmailService emailService;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // ==========================================
    // 1. REGISTRO (CON ENVÍO DE EMAIL REAL)
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Usuario nuevoUsuario) {
        logger.info(">>> Registro solicitado para: {}", nuevoUsuario.getCorreo());
        
        try {
            if (usuarioRepository.findByCorreo(nuevoUsuario.getCorreo()).isPresent()) {
                return ResponseEntity.status(400).body("El correo ya está registrado.");
            }

            // 1. Configuración del usuario
            nuevoUsuario.setPassword(passwordEncoder.encode(nuevoUsuario.getPassword()));
            nuevoUsuario.setFechaRegistro(LocalDateTime.now());
            nuevoUsuario.setRol(nuevoUsuario.getRol() == null ? "USER" : nuevoUsuario.getRol());
            nuevoUsuario.setTwoFactorEnabled(false);
            
            // 2. IMPORTANTE: Nace inactivo hasta confirmar
            nuevoUsuario.setActivo(false); 

            // 3. Generar Token
            String token = UUID.randomUUID().toString();
            nuevoUsuario.setConfirmationToken(token);

            // 4. Guardar en BD
            usuarioRepository.save(nuevoUsuario);

            // 5. ENVIAR EMAIL REAL
            String link = "http://127.0.0.1:5500/confirmar.html?token=" + token;
            String mensaje = "Hola " + nuevoUsuario.getNombreCompleto() + ",\n\n" +
                             "Gracias por registrarte en LEIGSeguros.\n" +
                             "Para activar tu cuenta, haz clic en el siguiente enlace:\n\n" +
                             link + "\n\n" +
                             "Si no has solicitado este registro, ignora este mensaje.";

            emailService.enviarCorreo(nuevoUsuario.getCorreo(), "Activa tu cuenta - LEIGSeguros", mensaje);
            
            logger.info("Email de activación enviado a: {}", nuevoUsuario.getCorreo());
            return ResponseEntity.ok("Usuario registrado. Se ha enviado un correo de activación.");

        } catch (Exception e) {
            logger.error("Error en registro", e);
            return ResponseEntity.status(500).body("Error al registrar: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. CONFIRMAR CUENTA
    // ==========================================
    @PostMapping("/confirm-account")
    public ResponseEntity<?> confirmAccount(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        Optional<Usuario> userOpt = usuarioRepository.findByConfirmationToken(token);

        if (userOpt.isPresent()) {
            Usuario usuario = userOpt.get();
            usuario.setActivo(true);
            usuario.setConfirmationToken(null); // Borramos el token para seguridad
            usuarioRepository.save(usuario);
            
            // Opcional: Enviar correo de bienvenida
            emailService.enviarCorreo(usuario.getCorreo(), "¡Bienvenido!", "Tu cuenta ha sido activada correctamente.");
            
            return ResponseEntity.ok("Cuenta activada correctamente.");
        } else {
            return ResponseEntity.status(400).body("Token inválido o expirado.");
        }
    }

    // ==========================================
    // 3. LOGIN (CON VALIDACIÓN DE ACTIVO Y 2FA)
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String correo = credenciales.get("correo");
        String password = credenciales.get("password");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                
                // CHECK: ¿ESTÁ ACTIVO?
                if (!usuario.getActivo()) {
                    return ResponseEntity.status(403).body("Tu cuenta no está activa. Revisa tu correo (mira Spam).");
                }

                // CHECK: 2FA
                if (Boolean.TRUE.equals(usuario.getTwoFactorEnabled())) {
                    Map<String, String> r = new HashMap<>();
                    r.put("status", "2FA_REQUIRED");
                    r.put("correo", correo);
                    return ResponseEntity.ok(r);
                }

                // LOGIN OK
                String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getRol(), usuario.getIdUsuario());
                Map<String, Object> resp = new HashMap<>();
                resp.put("token", token);
                resp.put("usuario", usuario);
                return ResponseEntity.ok(resp);
            }
        }
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }

    // ==========================================
    // 4. VERIFICAR CÓDIGO 2FA (LOGIN)
    // ==========================================
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2faLogin(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String codigoStr = body.get("codigo");
        try {
            int codigo = Integer.parseInt(codigoStr);
            Usuario usuario = usuarioRepository.findByCorreo(correo).orElseThrow();
            
            if (gAuth.authorize(usuario.getTwoFactorSecret(), codigo)) {
                String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getRol(), usuario.getIdUsuario());
                Map<String, Object> resp = new HashMap<>();
                resp.put("token", token);
                resp.put("usuario", usuario);
                return ResponseEntity.ok(resp);
            }
            return ResponseEntity.status(401).body("Código 2FA incorrecto");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error validando código");
        }
    }

    // ==========================================
    // 5. OLVIDÉ MI CONTRASEÑA (ENVÍO REAL)
    // ==========================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        Optional<Usuario> userOpt = usuarioRepository.findByCorreo(correo);
        
        if (userOpt.isPresent()) {
            Usuario usuario = userOpt.get();
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuarioRepository.save(usuario);

            // ENVÍO EMAIL REAL
            String link = "http://127.0.0.1:5500/restablecer.html?token=" + token;
            String mensaje = "Hola,\n\n" +
                             "Hemos recibido una solicitud para restablecer tu contraseña.\n" +
                             "Haz clic en el siguiente enlace para crear una nueva:\n\n" +
                             link + "\n\n" +
                             "Si no has pedido esto, ignora el mensaje.";

            emailService.enviarCorreo(correo, "Recuperación de Contraseña - LEIGSeguros", mensaje);
            logger.info("Email de recuperación enviado a: {}", correo);
        }
        // Siempre respondemos OK por seguridad (para no revelar qué correos existen)
        return ResponseEntity.ok("Si el correo existe, recibirás instrucciones.");
    }

    // ==========================================
    // 6. RESTABLECER CONTRASEÑA FINAL
    // ==========================================
    @PostMapping("/reset-password-token")
    public ResponseEntity<?> resetPasswordToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String nuevaPass = body.get("nuevaPassword");
        
        Optional<Usuario> userOpt = usuarioRepository.findByResetToken(token);
        if (userOpt.isPresent()) {
            Usuario u = userOpt.get();
            u.setPassword(passwordEncoder.encode(nuevaPass));
            u.setResetToken(null); // Quemamos el token
            usuarioRepository.save(u);
            
            emailService.enviarCorreo(u.getCorreo(), "Contraseña Cambiada", "Tu contraseña ha sido actualizada correctamente.");
            
            return ResponseEntity.ok("Contraseña actualizada con éxito.");
        }
        return ResponseEntity.status(400).body("El enlace es inválido o ha caducado.");
    }

    // ==========================================
    // 7. GESTIÓN 2FA (SETUP/CONFIRM/DISABLE)
    // ==========================================
    @PostMapping("/setup-2fa")
    public ResponseEntity<?> setup2fa(@RequestBody Map<String, String> body) {
        Usuario u = usuarioRepository.findByCorreo(body.get("correo")).orElseThrow();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        u.setTwoFactorSecret(key.getKey());
        usuarioRepository.save(u);
        
        Map<String, String> r = new HashMap<>();
        r.put("qrUrl", "otpauth://totp/LEIGSeguros:" + u.getCorreo() + "?secret=" + key.getKey() + "&issuer=LEIGSeguros");
        return ResponseEntity.ok(r);
    }

    @PostMapping("/confirm-2fa")
    public ResponseEntity<?> confirm2fa(@RequestBody Map<String, String> body) {
        Usuario u = usuarioRepository.findByCorreo(body.get("correo")).orElseThrow();
        int codigo = Integer.parseInt(body.get("codigo"));
        if (gAuth.authorize(u.getTwoFactorSecret(), codigo)) {
            u.setTwoFactorEnabled(true);
            usuarioRepository.save(u);
            return ResponseEntity.ok("2FA Activado");
        }
        return ResponseEntity.status(400).body("Código incorrecto");
    }

    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2fa(@RequestBody Map<String, String> body) {
        Usuario u = usuarioRepository.findByCorreo(body.get("correo")).orElseThrow();
        u.setTwoFactorEnabled(false);
        u.setTwoFactorSecret(null);
        usuarioRepository.save(u);
        return ResponseEntity.ok("2FA Desactivado");
    }
}