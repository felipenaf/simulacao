package com.example.simulacao.controller;

import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ErrorResponse;
import com.example.simulacao.exception.CpfJaCadastradoException;
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

        try {
            clienteService.save(request);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
        } catch (CpfJaCadastradoException e) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao criar novo cliente: {}", request, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }
}
