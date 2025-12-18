package com.aseguradora.api.controller;

import com.aseguradora.api.model.Factura;
import com.aseguradora.api.model.Seguro;
import com.aseguradora.api.repository.FacturaRepository;
import com.aseguradora.api.repository.SeguroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/seguros")
@CrossOrigin(origins = "*")
public class SeguroController {

    @Autowired
    private SeguroRepository seguroRepository;

    @Autowired
    private FacturaRepository facturaRepository; // <--- NECESARIO PARA CREAR FACTURAS

    @GetMapping
    public List<Seguro> listarTodos() { return seguroRepository.findAll(); }

    @GetMapping("/usuario/{idUsuario}")
    public List<Seguro> listarPorUsuario(@PathVariable Long idUsuario) {
        return seguroRepository.findByUsuario_IdUsuario(idUsuario);
    }

    // CREAR SEGURO + FACTURA AUTOMÁTICA
    @PostMapping
    public Seguro crearSeguro(@RequestBody Seguro seguro) {
        // 1. Guardamos el seguro primero
        Seguro nuevoSeguro = seguroRepository.save(seguro);

        // 2. Creamos la factura automáticamente
        Factura nuevaFactura = new Factura();
        nuevaFactura.setFechaEmision(LocalDate.now());
        nuevaFactura.setImporte(nuevoSeguro.getPrimaAnual());
        nuevaFactura.setConcepto("Alta Póliza: " + nuevoSeguro.getNumPoliza());
        nuevaFactura.setEstado("PENDIENTE"); // Nace pendiente de pago
        nuevaFactura.setSeguro(nuevoSeguro);
        nuevaFactura.setUsuario(nuevoSeguro.getUsuario()); // El mismo usuario del seguro

        facturaRepository.save(nuevaFactura);

        return nuevoSeguro;
    }

    @DeleteMapping("/{id}")
    public void borrarSeguro(@PathVariable Long id) {
        seguroRepository.deleteById(id);
    }
}