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

    @GetMapping
    public List<Factura> listarTodas() {
        return facturaRepository.findAll();
    }

    // Ver facturas de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public List<Factura> listarPorUsuario(@PathVariable Long idUsuario) {
        return facturaRepository.findByUsuario_IdUsuario(idUsuario);
    }

    @PostMapping
    public Factura crearFactura(@RequestBody Factura factura) {
        return facturaRepository.save(factura);
    }

    // ... otros métodos ...

    // BORRAR FACTURA
    @DeleteMapping("/{id}")
    public void borrarFactura(@PathVariable Long id) {
        facturaRepository.deleteById(id);
    }
}   