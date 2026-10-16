package com.example.simulacao.controller;

import com.example.simulacao.dto.SimulacaoRequest;
import com.example.simulacao.dto.SimulacaoResponse;
import com.example.simulacao.enums.FormatoExportacao;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.service.*;
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

@WebMvcTest(SimulacaoController.class)
class SimulacaoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimulacaoService simulacaoService;

    @MockitoBean
    private ExportadorSimulacaoFactory exportadorFactory;

    // GET /simulacoes/clientes/{id}

    @Test
    void deveListarSimulacoesEmJson() throws Exception {
        SimulacaoResponse simulacao = new SimulacaoResponse(
            LocalDateTime.of(2024, 6, 15, 10, 30, 26),
            new BigDecimal("300000.00"),
            new BigDecimal("1000000.00"),
            150,
            new BigDecimal("2.00")
        );

        // Cria o service e mocka o retorno
        when(simulacaoService.findByClienteId(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(simulacao)));

        // Chamada http
        mockMvc.perform(get("/simulacoes/clientes/1"))
            // Validações
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("valorSolicitado")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("dataHora")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("valorGarantia")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("quantidadeMeses")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("taxaJurosMensal")));

        // Valida se a chamada feita ao service foi exatamente essa
        verify(simulacaoService).findByClienteId(1L, PageRequest.of(0, 10));
    }

    @Test
    void deveListarSimulacoesEmCsv() throws Exception {
        SimulacaoResponse simulacao = new SimulacaoResponse(
            LocalDateTime.of(2026, 10, 15, 14, 13, 23),
            new BigDecimal("300000.01"),
            new BigDecimal("1000000.00"),
            420,
            new BigDecimal("2.00")
        );

        when(simulacaoService.findByClienteId(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(simulacao)));

        when(exportadorFactory.buscar(eq(FormatoExportacao.CSV)))
            .thenReturn(Optional.of(new ExportadorSimulacaoCsv()));

        mockMvc.perform(get("/simulacoes/clientes/1?formato=CSV&pagina=1"))
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

        verify(simulacaoService).findByClienteId(1L, PageRequest.of(0, 10));
    }

    @Test
    void deveListarSimulacoesEmTxt() throws Exception {
        SimulacaoResponse simulacao = new SimulacaoResponse(
            LocalDateTime.of(2026, 10, 15, 14, 13, 23),
            new BigDecimal("300000.01"),
            new BigDecimal("1000000.00"),
            420,
            new BigDecimal("2.00")
        );

        when(simulacaoService.findByClienteId(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(simulacao)));

        when(exportadorFactory.buscar(eq(FormatoExportacao.TXT)))
            .thenReturn(Optional.of(new ExportadorSimulacaoTxt(new ExportadorSimulacaoCsv())));

        mockMvc.perform(get("/simulacoes/clientes/1?formato=TXT&pagina=1"))
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

        verify(simulacaoService).findByClienteId(1L, PageRequest.of(0, 10));
    }

    @Test
    void deveListarSimulacoesPaginada() throws Exception {
        List<SimulacaoResponse> simulacoes = IntStream.range(0, 10)
            .mapToObj(i -> new SimulacaoResponse(
                LocalDateTime.of(2024, 6, 15, 10, 30, 26).plusDays(i),
                new BigDecimal("300000.00").add(new BigDecimal(i)),
                new BigDecimal("1000000.00"),
                150,
                new BigDecimal("2.00")
            ))
            .toList();

        when(simulacaoService.findByClienteId(eq(1L), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(
                simulacoes.subList(0, 2),
                PageRequest.of(0, 2),
                simulacoes.size()
            ));

        mockMvc.perform(get("/simulacoes/clientes/1?pagina=1&quantidade_por_pagina=2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].dataHora").isNotEmpty())
            .andExpect(jsonPath("$.content[1].dataHora").isNotEmpty())
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.pageSize").value(2))
            .andExpect(jsonPath("$.totalPages").value(5))
            .andExpect(jsonPath("$.first").value(true));

        verify(simulacaoService).findByClienteId(1L, PageRequest.of(0, 2));
    }

    @Test
    void deveRetornarNotFoundEmCasoDeClienteNaoExisteNoGet() throws Exception {
        when(simulacaoService.findByClienteId(eq(1L), any(Pageable.class)))
            .thenThrow(new ClienteNaoEncontradoException("Cliente não encontrado"));

        mockMvc.perform(get("/simulacoes/clientes/1"))
            .andExpect(status().isNotFound());

        verify(simulacaoService).findByClienteId(1L, PageRequest.of(0, 10));
    }

    // POST /simulacoes/clientes/{id}

    @Test
    void deveCadastrarSimulacoes() throws Exception {
        List<SimulacaoResponse> simulacoes = IntStream.range(0, 3)
            .mapToObj(i -> new SimulacaoResponse(
                LocalDateTime.of(2024, 6, 15, 10, 30, 26).plusDays(i),
                new BigDecimal("300000.00").add(new BigDecimal(i)),
                new BigDecimal("1000000.00"),
                150,
                new BigDecimal("2.00")
            ))
            .toList();

        List<SimulacaoRequest> requests = IntStream.range(0, 3)
            .mapToObj(i -> new SimulacaoRequest(
                new BigDecimal("300000.00").add(new BigDecimal(i)),
                new BigDecimal("1000000.00"),
                150,
                new BigDecimal("2.00")
            ))
            .toList();

        when(simulacaoService.save(eq(1L), anyList()))
            .thenReturn(simulacoes);

        mockMvc.perform(
            post("/simulacoes/clientes/1")
                .content(new ObjectMapper().writeValueAsString(requests))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.[0].dataHora").isNotEmpty())
            .andExpect(jsonPath("$.[1].dataHora").isNotEmpty())
            .andExpect(jsonPath("$.[2].dataHora").isNotEmpty());

        verify(simulacaoService).save(1L, requests);
    }

    @Test
    void deveRetornarNotFoundEmCasoDeClienteNaoExisteNoPost() throws Exception {
        List<SimulacaoRequest> requests = IntStream.range(0, 5)
            .mapToObj(i -> new SimulacaoRequest(
                new BigDecimal("300000.00").add(new BigDecimal(i)),
                new BigDecimal("1000000.00"),
                150,
                new BigDecimal("2.00")
            ))
            .toList();

        when(simulacaoService.save(eq(1L), anyList()))
            .thenThrow(new ClienteNaoEncontradoException("Cliente não encontrado"));

        mockMvc.perform(post("/simulacoes/clientes/1")
            .content(new ObjectMapper().writeValueAsString(requests))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());

        verify(simulacaoService).save(1L, requests);
    }
}