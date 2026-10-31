package com.example.simulation.controller;

import com.example.simulation.dto.SimulationRequest;
import com.example.simulation.dto.SimulationResponse;
import com.example.simulation.enums.FormatoExportacao;
import com.example.simulation.exception.CustomerNotFoundException;
import com.example.simulation.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SimulationController.class)
class SimulationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimulationService simulationService;

    @MockitoBean
    private ExportadorSimulacaoFactory exportadorFactory;

    // GET /simulations/customers/{id}

    @Test
    void deveListarSimulacoesEmJson() throws Exception {
        SimulationResponse simulation = new SimulationResponse(
            LocalDateTime.of(2024, 6, 15, 10, 30, 26),
            new BigDecimal("300000.00"),
            new BigDecimal("1000000.00"),
            150,
            new BigDecimal("2.00")
        );

        // Cria o service e mocka o retorno
        when(simulationService.findByCustomerId(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(simulation)));

        // Chamada http
        mockMvc.perform(get("/simulations/customers/1"))
            // Validações
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("valorSolicitado")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("dataHora")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("valorGarantia")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("quantidadeMeses")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("taxaJurosMensal")));

        // Valida se a chamada feita ao service foi exatamente essa
        verify(simulationService).findByCustomerId(1L, PageRequest.of(0, 10));
    }

    @Test
    void deveListarSimulacoesEmCsv() throws Exception {
        SimulationResponse simulation = new SimulationResponse(
            LocalDateTime.of(2026, 10, 15, 14, 13, 23),
            new BigDecimal("300000.01"),
            new BigDecimal("1000000.00"),
            420,
            new BigDecimal("2.00")
        );

        when(simulationService.findByCustomerId(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(simulation)));

        when(exportadorFactory.buscar(eq(FormatoExportacao.CSV)))
            .thenReturn(Optional.of(new ExportadorSimulacaoCsv()));

        mockMvc.perform(get("/simulations/customers/1?formato=CSV&pagina=1"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/csv"))
            .andExpect(content().string(
                org.hamcrest.Matchers.startsWith(
                    "dataHora;valorSolicitado;valorGarantia;quantidadeMeses;taxaJurosMensal"
                )
            ))
            .andExpect(content().string(
                org.hamcrest.Matchers.containsString(
                    "\"2026-10-15 14:13:23\";300000.01;1000000.00;420;2.00"
                )
            ));

        verify(simulationService).findByCustomerId(1L, PageRequest.of(0, 10));
    }

    @Test
    void deveListarSimulacoesEmTxt() throws Exception {
        SimulationResponse simulation = new SimulationResponse(
            LocalDateTime.of(2026, 10, 15, 14, 13, 23),
            new BigDecimal("300000.01"),
            new BigDecimal("1000000.00"),
            420,
            new BigDecimal("2.00")
        );

        when(simulationService.findByCustomerId(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(simulation)));

        when(exportadorFactory.buscar(eq(FormatoExportacao.TXT)))
            .thenReturn(Optional.of(new ExportadorSimulacaoTxt(new ExportadorSimulacaoCsv())));

        mockMvc.perform(get("/simulations/customers/1?formato=TXT&pagina=1"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/plain"))
            .andExpect(content().string(
                org.hamcrest.Matchers.startsWith(
                    "dataHora;valorSolicitado;valorGarantia;quantidadeMeses;taxaJurosMensal"
                )
            ))
            .andExpect(content().string(
                org.hamcrest.Matchers.containsString(
                    "\"2026-10-15 14:13:23\";300000.01;1000000.00;420;2.00"
                )
            ));

        verify(simulationService).findByCustomerId(1L, PageRequest.of(0, 10));
    }

    @Test
    void deveListarSimulacoesPaginada() throws Exception {
        List<SimulationResponse> simulations = IntStream.range(0, 10)
            .mapToObj(i -> new SimulationResponse(
                LocalDateTime.of(2024, 6, 15, 10, 30, 26).plusDays(i),
                new BigDecimal("300000.00").add(new BigDecimal(i)),
                new BigDecimal("1000000.00"),
                150,
                new BigDecimal("2.00")
            )).toList();

        when(simulationService.findByCustomerId(eq(1L), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(
                simulations.subList(0, 2),
                PageRequest.of(0, 2),
                simulations.size()
            ));

        mockMvc.perform(get("/simulations/customers/1?pagina=1&quantidade_por_pagina=2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].dataHora").isNotEmpty())
            .andExpect(jsonPath("$.content[1].dataHora").isNotEmpty())
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.pageSize").value(2))
            .andExpect(jsonPath("$.totalPages").value(5))
            .andExpect(jsonPath("$.first").value(true));

        verify(simulationService).findByCustomerId(1L, PageRequest.of(0, 2));
    }

    @Test
    void deveRetornarNotFoundEmCasoDeCustomerNaoExisteNoGet() throws Exception {
        when(simulationService.findByCustomerId(eq(1L), any(Pageable.class)))
            .thenThrow(new CustomerNotFoundException("Customer não encontrado"));

        mockMvc.perform(get("/simulations/customers/1"))
            .andExpect(status().isNotFound());

        verify(simulationService).findByCustomerId(1L, PageRequest.of(0, 10));
    }

    // POST /simulations/customers/{id}

    @Test
    void deveCadastrarSimulacoes() throws Exception {
        List<SimulationResponse> simulations = IntStream.range(0, 3)
            .mapToObj(i -> new SimulationResponse(
                LocalDateTime.of(2024, 6, 15, 10, 30, 26).plusDays(i),
                new BigDecimal("300000.00").add(new BigDecimal(i)),
                new BigDecimal("1000000.00"),
                150,
                new BigDecimal("2.00")
            ))
            .toList();

        List<SimulationRequest> requests = IntStream.range(0, 3)
            .mapToObj(i -> new SimulationRequest(
                new BigDecimal("300000.00").add(new BigDecimal(i)),
                new BigDecimal("1000000.00"),
                150,
                new BigDecimal("2.00")
            ))
            .toList();

        when(simulationService.save(eq(1L), anyList()))
            .thenReturn(simulations);

        mockMvc.perform(
            post("/simulations/customers/1")
                .content(new ObjectMapper().writeValueAsString(requests))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.[0].dataHora").isNotEmpty())
            .andExpect(jsonPath("$.[1].dataHora").isNotEmpty())
            .andExpect(jsonPath("$.[2].dataHora").isNotEmpty());

        verify(simulationService).save(1L, requests);
    }

    @Test
    void deveRetornarNotFoundEmCasoDeCustomerNaoExisteNoPost() throws Exception {
        List<SimulationRequest> requests = IntStream.range(0, 5)
            .mapToObj(i -> new SimulationRequest(
                new BigDecimal("300000.00").add(new BigDecimal(i)),
                new BigDecimal("1000000.00"),
                150,
                new BigDecimal("2.00")
            ))
            .toList();

        when(simulationService.save(eq(1L), anyList()))
            .thenThrow(new CustomerNotFoundException("Customer não encontrado"));

        mockMvc.perform(post("/simulations/customers/1")
            .content(new ObjectMapper().writeValueAsString(requests))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());

        verify(simulationService).save(1L, requests);
    }
}