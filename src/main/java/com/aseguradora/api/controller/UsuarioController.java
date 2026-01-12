package com.aseguradora.api.controller;

import com.aseguradora.api.model.*;
import com.aseguradora.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional; // Importante
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenSeguridadRepository tokenRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private SeguroRepository seguroRepository;

    @Autowired
    private SiniestroRepository siniestroRepository;

    @Autowired
    private PasswordEncoder encoder;

    // 1. LISTAR TODOS LOS USUARIOS
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // 2. OBTENER UN USUARIO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. ACTUALIZAR PERFIL (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario datosNuevos) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombreCompleto(datosNuevos.getNombreCompleto());
            usuario.setMovil(datosNuevos.getMovil());
            // No actualizamos correo/rol/pass aquí por seguridad
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(usuario);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. CAMBIAR CONTRASEÑA (Desde Configuración)
    @PostMapping("/{id}/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String oldPass = payload.get("oldPassword");
        String newPass = payload.get("newPassword");

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar contraseña antigua
        if (!encoder.matches(oldPass, usuario.getPassword())) {
            return ResponseEntity.badRequest().body("La contraseña actual no es correcta.");
        }

        usuario.setPassword(encoder.encode(newPass));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }

    // 5. BORRAR USUARIO (¡ARREGLADO!)
    // Este método borra todo lo asociado al usuario antes de borrarlo a él
    @DeleteMapping("/{id}")
    @Transactional // Vital para que si algo falla, no se borre nada a medias
    public ResponseEntity<?> borrarUsuario(@PathVariable Long id) {
        // A. PROTEGER AL ADMIN PRINCIPAL
        if (id == 1) {
            return ResponseEntity.status(403).body("No se puede eliminar al Administrador Principal.");
        }

        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // B. LIMPIEZA DE DATOS ASOCIADOS (Cascada Manual)
        
        // 1. Borrar Tokens de seguridad
        List<TokenSeguridad> tokens = tokenRepository.findAll().stream()
                .filter(t -> t.getUsuario().getIdUsuario().equals(id))
                .collect(Collectors.toList());
        tokenRepository.deleteAll(tokens);

        // 2. Borrar Facturas
        List<Factura> facturas = facturaRepository.findAll().stream()
                .filter(f -> f.getUsuario().getIdUsuario().equals(id))
                .collect(Collectors.toList());
        facturaRepository.deleteAll(facturas);

        // 3. Borrar Seguros (y sus Siniestros)
        List<Seguro> seguros = seguroRepository.findAll().stream()
                .filter(s -> s.getUsuario().getIdUsuario().equals(id))
                .collect(Collectors.toList());

        for (Seguro seguro : seguros) {
            // Antes de borrar el seguro, borramos sus siniestros
            List<Siniestro> siniestros = siniestroRepository.findAll().stream()
                    .filter(sin -> sin.getSeguro() != null && sin.getSeguro().getIdSeguro().equals(seguro.getIdSeguro()))
                    .collect(Collectors.toList());
            siniestroRepository.deleteAll(siniestros);
        }
        seguroRepository.deleteAll(seguros);

        // C. FINALMENTE BORRAR AL USUARIO
        usuarioRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}