package com.aseguradora.api.controller;

import com.aseguradora.api.model.Factura;
import com.aseguradora.api.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "*")
public class FacturaController {

    @Autowired
    private FacturaRepository facturaRepository;

    // 1. LISTAR TODAS (Para el ADMIN)
    @GetMapping
    public List<Factura> listarTodas() {
        return facturaRepository.findAll();
    }

    // 2. LISTAR POR USUARIO (Para el Cliente)
    @GetMapping("/usuario/{idUsuario}")
    public List<Factura> listarPorUsuario(@PathVariable Long idUsuario) {
        return facturaRepository.findByUsuario_IdUsuario(idUsuario);
    }

    // 3. CREAR FACTURA
    @PostMapping
    public Factura crearFactura(@RequestBody Factura factura) {
        return facturaRepository.save(factura);
    }

    // 4. BORRAR FACTURA
    @DeleteMapping("/{id}")
    public void borrarFactura(@PathVariable Long id) {
        facturaRepository.deleteById(id);
    }
}