package com.aseguradora.api.controller;

import com.aseguradora.api.dto.LoginRequest;
import com.aseguradora.api.model.TokenSeguridad;
import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.repository.TokenSeguridadRepository;
import com.aseguradora.api.repository.UsuarioRepository;
import com.aseguradora.api.service.EmailService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
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
import java.util.UUID;

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
    @Autowired
    private EmailService emailService;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // --- 1. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(loginRequest.getCorreo())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // --- NUEVO REQUISITO: VERIFICAR SI ESTÁ ACTIVO ---
            if (Boolean.FALSE.equals(usuario.getActivo())) {
                return ResponseEntity.status(403).body("Debes confirmar tu correo electrónico antes de entrar.");
            }

            // Validar contraseña
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getCorreo(), loginRequest.getPassword())
            );

            // Reseteamos intentos 2FA si entra bien
            usuario.setIntentos2fa(0); 
            usuarioRepository.save(usuario);

            // Comprobar 2FA
            if (Boolean.TRUE.equals(usuario.getTwoFactorEnabled())) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "2FA_REQUIRED");
                resp.put("mensaje", "Introduce el código de tu aplicación Authenticator.");
                resp.put("correo", usuario.getCorreo());
                return ResponseEntity.ok(resp);
            }

            return realizarLoginStandard(usuario, request, response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Credenciales incorrectas o cuenta no activa");
        }
    }

    // --- 2. VERIFICAR CÓDIGO 2FA ---
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2fa(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpServletResponse response) {
        String correo = payload.get("correo");
        int codigo;
        try { codigo = Integer.parseInt(payload.get("codigo")); } catch (Exception e) { return ResponseEntity.badRequest().body("Código inválido"); }

        Usuario usuario = usuarioRepository.findByCorreo(correo).orElseThrow();

        if (usuario.getIntentos2fa() != null && usuario.getIntentos2fa() >= 3) {
            return ResponseEntity.status(403).body("Cuenta bloqueada temporalmente.");
        }

        if (gAuth.authorize(usuario.getTwoFactorSecret(), codigo)) {
            usuario.setIntentos2fa(0);
            usuarioRepository.save(usuario);
            return realizarLoginStandard(usuario, request, response);
        } else {
            int intentos = (usuario.getIntentos2fa() == null ? 0 : usuario.getIntentos2fa()) + 1;
            usuario.setIntentos2fa(intentos);
            usuarioRepository.save(usuario);
            if (intentos >= 3) return ResponseEntity.status(403).body("Has superado los 3 intentos.");
            return ResponseEntity.badRequest().body("Código incorrecto.");
        }
    }

    // --- 3. SETUP y CONFIRM 2FA (Sin cambios) ---
    @PostMapping("/setup-2fa")
    public ResponseEntity<?> setup2fa(@RequestBody Map<String, String> payload) {
        Usuario u = usuarioRepository.findByCorreo(payload.get("correo")).orElseThrow();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        u.setTwoFactorSecret(key.getKey());
        u.setTwoFactorEnabled(false);
        usuarioRepository.save(u);
        return ResponseEntity.ok(Map.of("secret", key.getKey(), "qrUrl", GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL("AseguradoraApp", u.getCorreo(), key)));
    }

    @PostMapping("/confirm-2fa")
    public ResponseEntity<?> confirm2fa(@RequestBody Map<String, String> payload) {
        Usuario u = usuarioRepository.findByCorreo(payload.get("correo")).orElseThrow();
        if (gAuth.authorize(u.getTwoFactorSecret(), Integer.parseInt(payload.get("codigo")))) {
            u.setTwoFactorEnabled(true);
            usuarioRepository.save(u);
            return ResponseEntity.ok("2FA Activado");
        }
        return ResponseEntity.badRequest().body("Código erróneo");
    }

    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2fa(@RequestBody Map<String, String> payload) {
        Usuario u = usuarioRepository.findByCorreo(payload.get("correo")).orElseThrow();
        u.setTwoFactorEnabled(false);
        u.setTwoFactorSecret(null);
        usuarioRepository.save(u);
        return ResponseEntity.ok("2FA Desactivado");
    }

    // --- 4. RECUPERACIÓN PASSWORD (Sin cambios) ---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        Usuario u = usuarioRepository.findByCorreo(payload.get("correo")).orElse(null);
        if (u != null) {
            String tokenStr = UUID.randomUUID().toString();
            TokenSeguridad t = new TokenSeguridad();
            t.setToken(tokenStr);
            t.setUsuario(u);
            t.setTipo("RECUPERACION");
            t.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
            tokenRepository.save(t);

            String url = "http://127.0.0.1:5500/restablecer.html?token=" + tokenStr;
            emailService.enviarCorreo(u.getCorreo(), "Restablecer Contraseña", "Haz clic aquí: " + url);
        }
        return ResponseEntity.ok("Correo enviado si existe.");
    }

    @PostMapping("/reset-password-token")
    public ResponseEntity<?> resetPasswordToken(@RequestBody Map<String, String> payload) {
        TokenSeguridad t = tokenRepository.findByToken(payload.get("token")).orElseThrow(() -> new RuntimeException("Token inválido"));
        if (t.getFechaExpiracion().isBefore(LocalDateTime.now())) return ResponseEntity.status(400).body("Token expirado");
        
        Usuario u = t.getUsuario();
        u.setPassword(encoder.encode(payload.get("nuevaPassword")));
        usuarioRepository.save(u);
        tokenRepository.delete(t);
        return ResponseEntity.ok("Contraseña cambiada");
    }

    // --- 5. REGISTRO CON CONFIRMACIÓN (MODIFICADO) ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario nuevoUsuario) {
        if (usuarioRepository.findByCorreo(nuevoUsuario.getCorreo()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: El correo ya está registrado.");
        }
        Usuario user = new Usuario();
        user.setNombreCompleto(nuevoUsuario.getNombreCompleto());
        user.setCorreo(nuevoUsuario.getCorreo());
        user.setPassword(encoder.encode(nuevoUsuario.getPassword()));
        user.setRol(nuevoUsuario.getRol());
        user.setFechaRegistro(LocalDateTime.now());
        user.setTwoFactorEnabled(false);
        user.setIntentos2fa(0);
        user.setMovil(nuevoUsuario.getMovil() != null ? nuevoUsuario.getMovil() : "");
        user.setDireccion("");

        // CAMBIO IMPORTANTE: Nace INACTIVO
        user.setActivo(false); 
        Usuario usuarioGuardado = usuarioRepository.save(user);

        // Generar Token de Activación
        String tokenStr = UUID.randomUUID().toString();
        TokenSeguridad token = new TokenSeguridad();
        token.setToken(tokenStr);
        token.setUsuario(usuarioGuardado);
        token.setTipo("ACTIVACION"); // Usamos un tipo diferente para distinguir
        token.setFechaExpiracion(LocalDateTime.now().plusHours(24)); // 24 horas para activar
        tokenRepository.save(token);

        // Enviar Correo de Activación
        String enlace = "http://127.0.0.1:5500/confirmar.html?token=" + tokenStr;
        emailService.enviarCorreo(
            user.getCorreo(),
            "Activa tu cuenta - Aseguradora",
            "Bienvenido " + user.getNombreCompleto() + ",\n\n" +
            "Para activar tu cuenta, haz clic en el siguiente enlace:\n\n" +
            enlace + "\n\n" +
            "Si no te has registrado tú, ignora este correo."
        );

        return ResponseEntity.ok("Registro exitoso. Revisa tu correo para activar la cuenta.");
    }

    // --- 6. ENDPOINT PARA CONFIRMAR CUENTA (NUEVO) ---
    @PostMapping("/confirm-account")
    public ResponseEntity<?> confirmAccount(@RequestBody Map<String, String> payload) {
        String tokenStr = payload.get("token");
        TokenSeguridad tokenDB = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (!"ACTIVACION".equals(tokenDB.getTipo())) {
            return ResponseEntity.badRequest().body("Este token no es de activación.");
        }

        Usuario usuario = tokenDB.getUsuario();
        usuario.setActivo(true); // ¡ACTIVAMOS AL USUARIO!
        usuarioRepository.save(usuario);
        
        tokenRepository.delete(tokenDB); // Borramos el token usado

        return ResponseEntity.ok("Cuenta activada correctamente.");
    }

    // Auxiliar Login
    private ResponseEntity<?> realizarLoginStandard(Usuario usuario, HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                usuario.getCorreo(), null, org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_" + usuario.getRol()));
        SecurityContext sc = SecurityContextHolder.createEmptyContext();
        sc.setAuthentication(auth);
        SecurityContextHolder.setContext(sc);
        securityContextRepository.saveContext(sc, request, response);
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "OK");
        resp.put("usuario", usuario);
        return ResponseEntity.ok(resp);
    }
}