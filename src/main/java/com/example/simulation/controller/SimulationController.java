package com.example.simulation.controller;

import com.example.simulation.dto.*;
import com.example.simulation.enums.FormatoExportacao;
import com.example.simulation.exception.CustomerNotFoundException;
import com.example.simulation.service.ExportadorSimulacao;
import com.example.simulation.service.ExportadorSimulacaoFactory;
import com.example.simulation.service.SimulationService;
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
@RequestMapping("/simulations")
public class SimulationController {
    private final SimulationService simulationService;
    private final ExportadorSimulacaoFactory exportadorFactory;

    public SimulationController(
        SimulationService simulationService,
        ExportadorSimulacaoFactory exportadorFactory
    ) {
        this.simulationService = simulationService;
        this.exportadorFactory = exportadorFactory;
    }

    @PostMapping("/customers/{customerId}")
    public ResponseEntity<?> save(
        @PathVariable Long customerId,
        @RequestBody
        @Valid
        @Size(min = 1, max = 5, message = "A lista deve conter entre 1 e 5 simulações")
        List<@Valid SimulationRequest> requests
    ) {
        log.info("Criando {} simulações para o customer {}", requests.size(), customerId);

        try {
            List<SimulationResponse> response = simulationService.save(customerId, requests);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
        } catch (CustomerNotFoundException e) {
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
            Page<SimulationResponse> simulations = simulationService.findByCustomerId(
                customerId,
                // O índice da paginação inicia em zero
                PageRequest.of(pagina - 1, quantidadePorPagina)
            );

            return switch (formato) {
                case JSON -> ResponseEntity.ok(PageResponse.from(simulations));
                case CSV, TXT -> montaRespostaExportacao(simulations, customerId, formato);
            };
        } catch (CustomerNotFoundException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        } catch (Exception e) {
            log.error("Erro ao obter simulations do customer: {}", customerId, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    private ResponseEntity<byte[]> montaRespostaExportacao(
        Page<SimulationResponse> simulations,
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
            .body(exportador.exportar(simulations.getContent()));
    }
}