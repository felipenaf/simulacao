package com.example.simulacao.controller;

import com.example.simulacao.dto.SimulacaoResponse;
import com.example.simulacao.service.ExportadorSimulacaoFactory;
import com.example.simulacao.service.SimulacaoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(SimulacaoController.class)
class SimulacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimulacaoService simulacaoService;

    @MockitoBean
    private ExportadorSimulacaoFactory exportadorFactory;

    @Test
    void deveListarSimulacoesDoClienteEmJson() throws Exception {
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
}