package com.aseguradora.api.controller;

import com.aseguradora.api.model.Siniestro;
import com.aseguradora.api.repository.SiniestroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // <--- Importante para la respuesta
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/siniestros")
@CrossOrigin(origins = "*") // Permite conexiones desde cualquier frontend
public class SiniestroController {

    @Autowired
    private SiniestroRepository siniestroRepository;

    // 1. LISTAR TODOS LOS SINIESTROS (Para el Admin)
    @GetMapping
    public List<Siniestro> listarTodos() {
        return siniestroRepository.findAll();
    }

    // 2. OBTENER UN SINIESTRO POR ID (Para rellenar el formulario de edición)
    @GetMapping("/{id}")
    public ResponseEntity<Siniestro> obtenerSiniestro(@PathVariable Long id) {
        return siniestroRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. OBTENER SINIESTROS DE UN USUARIO (Para el Cliente)
    @GetMapping("/usuario/{idUsuario}")
    public List<Siniestro> listarPorUsuario(@PathVariable Long idUsuario) {
        return siniestroRepository.findBySeguro_Usuario_IdUsuario(idUsuario);
    }

    // 4. REPORTAR NUEVO SINIESTRO
    @PostMapping
    public Siniestro reportarSiniestro(@RequestBody Siniestro siniestro) {
        siniestro.setFechaSuceso(java.time.LocalDate.now());
        siniestro.setEstado("ABIERTO"); // Siempre nace abierto
        siniestro.setResolucion("Pendiente de asignación de perito.");
        return siniestroRepository.save(siniestro);
    }

    // 5. EDITAR SINIESTRO (PUT) - ¡ESTA ES LA QUE FALTABA!
    @PutMapping("/{id}")
    public ResponseEntity<Siniestro> updateSiniestro(@PathVariable Long id, @RequestBody Siniestro siniestroDetalles) {
        Siniestro siniestro = siniestroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Siniestro no encontrado con id: " + id));

        // Actualizamos estado y resolución (lo que toca el Admin)
        siniestro.setEstado(siniestroDetalles.getEstado());
        siniestro.setResolucion(siniestroDetalles.getResolucion());
        
        // Guardamos cambios
        final Siniestro updatedSiniestro = siniestroRepository.save(siniestro);
        return ResponseEntity.ok(updatedSiniestro);
    }
    
    // 6. BORRAR SINIESTRO
    @DeleteMapping("/{id}")
    public void borrarSiniestro(@PathVariable Long id) {
        siniestroRepository.deleteById(id);
    }
}