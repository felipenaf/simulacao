package com.example.simulacao.controller;

import com.example.simulacao.dto.*;
import com.example.simulacao.enums.FormatoExportacao;
import com.example.simulacao.exception.CustomerNaoEncontradoException;
import com.example.simulacao.service.ExportadorSimulacao;
import com.example.simulacao.service.ExportadorSimulacaoFactory;
import com.example.simulacao.service.SimulacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/simulacoes")
public class SimulacaoController {
    private final SimulacaoService simulacaoService;
    private final ExportadorSimulacaoFactory exportadorFactory;

    public SimulacaoController(
        SimulacaoService simulacaoService,
        ExportadorSimulacaoFactory exportadorFactory
    ) {
        this.simulacaoService = simulacaoService;
        this.exportadorFactory = exportadorFactory;
    }

    @PostMapping("/customers/{customerId}")
    public ResponseEntity<?> save(
        @PathVariable Long customerId,
        @RequestBody
        @Valid
        @Size(min = 1, max = 5, message = "A lista deve conter entre 1 e 5 simulações")
        List<@Valid SimulacaoRequest> requests
    ) {
        log.info("Criando {} simulações para o customer {}", requests.size(), customerId);

        try {
            List<SimulacaoResponse> response = simulacaoService.save(customerId, requests);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
        } catch (CustomerNaoEncontradoException e) {
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

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<?> findByCustomerId(
        @PathVariable Long customerId,

        @RequestParam(defaultValue = "JSON")
        FormatoExportacao formato,

        @RequestParam(defaultValue = "1")
        @Min(value = 1, message = "A página deve ser maior ou igual a 1")
        int pagina,

        @RequestParam(name = "quantidade_por_pagina", defaultValue = "10")
        @Min(value = 1, message = "A quantidade por página deve ser maior ou igual a 1")
        @Max(value = 100, message = "A quantidade máxima por página é 100")
        int quantidadePorPagina
    ) {
        try {
            Page<SimulacaoResponse> simulacoes = simulacaoService.findByCustomerId(
                customerId,
                // O índice da paginação inicia em zero
                PageRequest.of(pagina - 1, quantidadePorPagina)
            );

            return switch (formato) {
                case JSON -> ResponseEntity.ok(PageResponse.from(simulacoes));
                case CSV, TXT -> montaRespostaExportacao(simulacoes, customerId, formato);
            };
        } catch (CustomerNaoEncontradoException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        } catch (Exception e) {
            log.error("Erro ao obter simulacoes do customer: {}", customerId, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    private ResponseEntity<byte[]> montaRespostaExportacao(
        Page<SimulacaoResponse> simulacoes,
        Long customerId,
        FormatoExportacao formato
    ) throws IOException {
        Optional<ExportadorSimulacao> resultado = exportadorFactory.buscar(formato);

        if (resultado.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ExportadorSimulacao exportador = resultado.get();

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(exportador.contentType()))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + exportador.nomeArquivo(customerId) + "\""
            )
            .body(exportador.exportar(simulacoes.getContent()));
    }
}