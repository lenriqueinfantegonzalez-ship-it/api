package com.aseguradora.api.controller;

import com.aseguradora.api.model.Siniestro;
import com.aseguradora.api.repository.SiniestroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/siniestros")
@CrossOrigin(origins = "*")
public class SiniestroController {

    @Autowired
    private SiniestroRepository siniestroRepository;

    // Obtener siniestros de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public List<Siniestro> listarPorUsuario(@PathVariable Long idUsuario) {
        return siniestroRepository.findBySeguro_Usuario_IdUsuario(idUsuario);
    }

    // Reportar un nuevo siniestro
    @PostMapping
    public Siniestro reportarSiniestro(@RequestBody Siniestro siniestro) {
        siniestro.setFechaSuceso(java.time.LocalDate.now());
        siniestro.setEstado("ABIERTO"); // Siempre nace abierto
        siniestro.setResolucion("Pendiente de asignación de perito.");
        return siniestroRepository.save(siniestro);
    }
    
    // Borrar siniestro (opcional)
    @DeleteMapping("/{id}")
    public void borrarSiniestro(@PathVariable Long id) {
        siniestroRepository.deleteById(id);
    }
}