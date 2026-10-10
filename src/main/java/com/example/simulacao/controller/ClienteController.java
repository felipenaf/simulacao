package com.example.simulacao.controller;

import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.service.ClienteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody ClienteRequest request){
        log.info("Criando novo cliente: {}", request.toString());

        clienteService.save(request);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
