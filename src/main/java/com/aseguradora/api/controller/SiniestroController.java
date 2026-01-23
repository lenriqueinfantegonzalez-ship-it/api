package com.aseguradora.api.controller;

import com.aseguradora.api.model.Siniestro;
import com.aseguradora.api.repository.SiniestroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/siniestros")
@CrossOrigin(origins = "*") // Permite conexiones desde cualquier frontend
public class SiniestroController {

    @Autowired
    private SiniestroRepository siniestroRepository;

    // 1. LISTAR TODOS LOS SINIESTROS (NUEVO: Para el Admin)
    @GetMapping
    public List<Siniestro> listarTodos() {
        return siniestroRepository.findAll();
    }

    // 2. OBTENER SINIESTROS DE UN USUARIO (Para el Cliente)
    @GetMapping("/usuario/{idUsuario}")
    public List<Siniestro> listarPorUsuario(@PathVariable Long idUsuario) {
        return siniestroRepository.findBySeguro_Usuario_IdUsuario(idUsuario);
    }

    // 3. REPORTAR NUEVO SINIESTRO
    @PostMapping
    public Siniestro reportarSiniestro(@RequestBody Siniestro siniestro) {
        siniestro.setFechaSuceso(java.time.LocalDate.now());
        siniestro.setEstado("ABIERTO"); // Siempre nace abierto
        siniestro.setResolucion("Pendiente de asignación de perito.");
        return siniestroRepository.save(siniestro);
    }
    
    // 4. BORRAR SINIESTRO
    @DeleteMapping("/{id}")
    public void borrarSiniestro(@PathVariable Long id) {
        siniestroRepository.deleteById(id);
    }
    // --- NUEVO: EDITAR SINIESTRO (PUT) ---
    @PutMapping("/{id}")
    public ResponseEntity<Siniestro> updateSiniestro(@PathVariable Long id, @RequestBody Siniestro siniestroDetalles) {
        Siniestro siniestro = siniestroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Siniestro no encontrado con id: " + id));

        // Actualizamos estado y resolución
        siniestro.setEstado(siniestroDetalles.getEstado());
        siniestro.setResolucion(siniestroDetalles.getResolucion());
        
        final Siniestro updatedSiniestro = siniestroRepository.save(siniestro);
        return ResponseEntity.ok(updatedSiniestro);
    }
}