package com.example.simulacao.controller;

import com.example.simulacao.dto.*;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.service.SimulacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/simulacoes")
public class SimulacaoController {
    private final SimulacaoService simulacaoService;

    public SimulacaoController(SimulacaoService simulacaoService) {
        this.simulacaoService = simulacaoService;
    }

    @PostMapping("/clientes/{clienteId}")
    public ResponseEntity<?> criar(
        @PathVariable Long clienteId,
        @RequestBody
        @Valid
        @Size(min = 1, max = 5, message = "A lista deve conter entre 1 e 5 simulações")
        List<@Valid SimulacaoRequest> requests
    ) {
        log.info("Criando {} simulações para o cliente {}", requests.size(), clienteId);

        try {
            List<SimulacaoResponse> response = simulacaoService.save(clienteId, requests);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
        } catch (ClienteNaoEncontradoException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao criar simulações: {}", requests, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    @GetMapping("/clientes/{clienteId}")
    public ResponseEntity<?> findById(
        @PathVariable Long clienteId,
        @RequestParam(defaultValue = "1")
        @Min(value = 1, message = "A página deve ser maior ou igual a 1")
        int pagina,

        @RequestParam(name = "quantidade_por_pagina", defaultValue = "10")
        @Min(value = 1, message = "A quantidade por página deve ser maior ou igual a 1")
        @Max(value = 100, message = "A quantidade máxima por página é 100")
        int quantidadePorPagina
    ) {
        try {
            Page<SimulacaoResponse> simulacoes = simulacaoService.findByClienteId(
                clienteId,
                // O índice da paginação inicia em zero
                PageRequest.of(pagina - 1, quantidadePorPagina)
            );

            return ResponseEntity.ok(PageResponse.from(simulacoes));
        } catch (ClienteNaoEncontradoException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        } catch (Exception e) {
            log.error("Erro ao obter simulacoes do cliente: {}", clienteId, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }
}