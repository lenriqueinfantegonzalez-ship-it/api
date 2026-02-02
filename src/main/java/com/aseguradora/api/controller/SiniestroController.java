package com.aseguradora.api.controller;

import com.aseguradora.api.model.Siniestro;
import com.aseguradora.api.model.Usuario;
import com.aseguradora.api.model.Seguro;
import com.aseguradora.api.repository.SiniestroRepository;
import com.aseguradora.api.repository.UsuarioRepository;
import com.aseguradora.api.repository.SeguroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/siniestros")
@CrossOrigin(origins = "*") 
public class SiniestroController {

    @Autowired
    private SiniestroRepository siniestroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SeguroRepository seguroRepository;

    // 1. LISTAR TODOS
    @GetMapping
    public List<Siniestro> getAllSiniestros() {
        return siniestroRepository.findAll();
    }

    // 2. OBTENER POR USUARIO
    @GetMapping("/usuario/{id}")
    public ResponseEntity<?> getSiniestrosPorUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        List<Siniestro> filtrados = siniestroRepository.findAll().stream()
                .filter(s -> s.getUsuario() != null && s.getUsuario().getIdUsuario().equals(id))
                .toList();
        return ResponseEntity.ok(filtrados);
    }
    
    // 3. OBTENER UN SINIESTRO POR ID (ARREGLADO: Sin usar orElse conflictivo)
    @GetMapping("/{id}")
    public ResponseEntity<?> getSiniestroById(@PathVariable Long id) {
        Optional<Siniestro> siniestroOpt = siniestroRepository.findById(id);
        
        if (siniestroOpt.isPresent()) {
            return ResponseEntity.ok(siniestroOpt.get());
        } else {
            return ResponseEntity.status(404).body("Siniestro no encontrado");
        }
    }

    // 4. CREAR SINIESTRO
    @PostMapping
    public ResponseEntity<?> crearSiniestro(@RequestBody Map<String, Object> payload) {
        try {
            Siniestro siniestro = new Siniestro();

            String fechaStr = (String) payload.get("fecha"); 
            if (fechaStr != null && !fechaStr.isEmpty()) {
                siniestro.setFechaSuceso(LocalDate.parse(fechaStr));
            } else {
                siniestro.setFechaSuceso(LocalDate.now());
            }

            siniestro.setDescripcion((String) payload.get("descripcion"));
            siniestro.setEstado("ABIERTO");
            siniestro.setResolucion("Pendiente de revisión");

            // Vincular Seguro y Usuario
            Seguro seguroEncontrado = null;
            if (payload.containsKey("seguro")) {
                Map<String, Object> seguroMap = (Map<String, Object>) payload.get("seguro");
                Object idObj = seguroMap.get("idSeguro");
                if (idObj != null) {
                    Long idSeguro = Long.valueOf(idObj.toString());
                    Optional<Seguro> seguroOpt = seguroRepository.findById(idSeguro);
                    if (seguroOpt.isPresent()) {
                        seguroEncontrado = seguroOpt.get();
                        siniestro.setSeguro(seguroEncontrado);
                    }
                }
            }

            if (payload.containsKey("idUsuario")) {
                Long idUsuario = Long.valueOf(payload.get("idUsuario").toString());
                Optional<Usuario> u = usuarioRepository.findById(idUsuario);
                if (u.isPresent()) siniestro.setUsuario(u.get());
            } else if (seguroEncontrado != null) {
                siniestro.setUsuario(seguroEncontrado.getUsuario());
            } else {
                return ResponseEntity.status(400).body("Error: Falta usuario o seguro.");
            }

            siniestroRepository.save(siniestro);
            return ResponseEntity.ok("Siniestro registrado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    // 5. ACTUALIZAR (ARREGLADO: Sin usar map().orElse() para evitar conflicto de tipos)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarSiniestro(@PathVariable Long id, @RequestBody Siniestro datos) {
        Optional<Siniestro> sOpt = siniestroRepository.findById(id);

        if (sOpt.isPresent()) {
            Siniestro s = sOpt.get();
            if (datos.getEstado() != null) s.setEstado(datos.getEstado());
            if (datos.getResolucion() != null) s.setResolucion(datos.getResolucion());
            
            siniestroRepository.save(s);
            return ResponseEntity.ok(s);
        } else {
            return ResponseEntity.status(404).body("Siniestro no encontrado.");
        }
    }

    // 6. BORRAR
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSiniestro(@PathVariable Long id) {
        if (siniestroRepository.existsById(id)) {
            siniestroRepository.deleteById(id);
            return ResponseEntity.ok("Siniestro eliminado.");
        }
        return ResponseEntity.status(404).body("No existe ese siniestro.");
    }
}