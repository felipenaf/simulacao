package com.example.simulacao.controller;

import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ClienteResponse;
import com.example.simulacao.dto.ErrorResponse;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.exception.CpfJaCadastradoException;
import com.example.simulacao.service.ClienteService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

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
                .body(new ErrorResponse(List.of(e.getMessage())));
        } catch (Exception e) {
            log.error("Erro ao criar novo cliente: {}", request, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clienteService.findById(id));
        } catch (ClienteNaoEncontradoException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        } catch (Exception e) {
            log.error("Erro ao obter o cliente: {}", id, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    @GetMapping()
    public ResponseEntity<?> findAll(
        @RequestParam(defaultValue = "1")
        @Min(value = 1, message = "A página deve ser maior ou igual a 1")
        int pagina,

        @RequestParam(name = "quantidade_por_pagina", defaultValue = "10")
        @Min(value = 1, message = "A quantidade por página deve ser maior ou igual a 1")
        @Max(value = 100, message = "A quantidade máxima por página é 100")
        int quantidadePorPagina
    ) {
        try {
            // O índice da paginação inicia em zero
            int paginaCorrigida = pagina - 1;

            Page<ClienteResponse> list = clienteService.findAll(
                PageRequest.of(paginaCorrigida, quantidadePorPagina)
            );

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Erro ao obter lista de clientes: ", e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
        @PathVariable Long id,
        @Valid @RequestBody ClienteRequest request
    ) {
        clienteService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
