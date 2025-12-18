package com.aseguradora.api.controller;

import com.aseguradora.api.model.TipoSeguro;
import com.aseguradora.api.repository.TipoSeguroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-seguro")
@CrossOrigin(origins = "*")
public class TipoSeguroController {

    @Autowired
    private TipoSeguroRepository tipoSeguroRepository;

    @GetMapping
    public List<TipoSeguro> listarTipos() {
        return tipoSeguroRepository.findAll();
    }

    // Para crear tipos nuevos (útil para el Admin)
    @PostMapping
    public TipoSeguro crearTipo(@RequestBody TipoSeguro tipo) {
        return tipoSeguroRepository.save(tipo);
    }
}