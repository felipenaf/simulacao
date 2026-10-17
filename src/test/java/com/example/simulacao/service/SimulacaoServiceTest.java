package com.example.simulacao.service;

import com.example.simulacao.dto.SimulacaoRequest;
import com.example.simulacao.dto.SimulacaoResponse;
import com.example.simulacao.entity.Cliente;
import com.example.simulacao.entity.Simulacao;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.repository.ClienteRepository;
import com.example.simulacao.repository.SimulacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class SimulacaoServiceTest {
    //    | # | Cenário | Resultado esperado | Prioridade |
    //|---|---|---|---|
    //| 1 | Cadastrar uma simulação para um cliente existente | Retorna uma lista com a simulação cadastrada | Alta |
    //| 2 | Cadastrar várias simulações para um cliente existente | Retorna todas as simulações cadastradas | Alta |
    //| 3 | Tentar cadastrar simulações para um cliente inexistente | Lança `ClienteNaoEncontradoException` | Alta |
    //| 4 | Cadastrar simulações para um cliente existente | Todas as entidades são associadas ao cliente correto | Alta |
    //| 5 | Cadastrar simulações para um cliente existente | Chama `simulacaoRepository.saveAll()` uma vez com todas as entidades | Alta |
    //| 6 | Tentar cadastrar simulações para um cliente inexistente | Não chama `simulacaoRepository.saveAll()` | Alta |
    //| 7 | Cadastrar uma simulação | Os campos são copiados corretamente de `SimulacaoRequest` para `Simulacao` | Alta |
    //| 8 | Cadastrar uma simulação | O retorno contém os dados esperados em `SimulacaoResponse` | Alta |
    //| 9 | Enviar uma lista vazia diretamente ao serviço | Verificar o comportamento atual: salvar uma lista vazia e retornar uma lista vazia | Média |

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SimulacaoRepository simulacaoRepository;

    @InjectMocks
    private SimulacaoServiceImpl simulacaoService;

    @Captor
    private ArgumentCaptor<List<Simulacao>> simulacoesCaptor;

    @Test
    void deveCadastrarSimulacao() {
        SimulacaoRequest request = new SimulacaoRequest(
            new BigDecimal("30000"),
            new BigDecimal("50000.50"),
            420,
            new BigDecimal("2.5")
        );

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(new Cliente()));

        when(simulacaoRepository.saveAll(anyList()))
            // invocation.getArgument(0) é o objeto que SimulacaoRepository
            // retorna se baseando nos dados da request
            .thenAnswer(invocation -> invocation.getArgument(0));

        List<SimulacaoResponse> response =
            simulacaoService.save(1L, List.of(request));

        assertEquals(1, response.size());
        assertEquals(new BigDecimal("30000"), response.getFirst().valorSolicitado());

        verify(clienteRepository).findById(1L);
        verify(simulacaoRepository).saveAll(anyList());
    }

    @Test
    void deveCadastrarSimulacoesSimples() {
        Cliente cliente = new Cliente();
        List<SimulacaoRequest> requests = IntStream.range(0, 3)
            .mapToObj(i -> new SimulacaoRequest(
                new BigDecimal("30000").add(new BigDecimal(i)),
                new BigDecimal("50000.50"),
                420,
                new BigDecimal("2.5")
            )).toList();

        List<Simulacao> simulacoes = requests.stream()
            .map(request ->
                Simulacao.builder()
                    .cliente(cliente)
                    .valorSolicitado(request.valorSolicitado())
                    .valorGarantia(request.valorGarantia())
                    .quantidadeMeses(request.quantidadeMeses())
                    .taxaJurosMensal(request.taxaJurosMensal())
                    .build()
            ).toList();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(simulacaoRepository.saveAll(anyList())).thenReturn(simulacoes);

        List<SimulacaoResponse> response = simulacaoService.save(1L, requests);

        assertNotNull(response);
        assertEquals(3, response.size());
        assertEquals(new BigDecimal("30000"), response.get(0).valorSolicitado());
        assertEquals(new BigDecimal("30001"), response.get(1).valorSolicitado());
        assertEquals(new BigDecimal("30002"), response.get(2).valorSolicitado());

        verify(clienteRepository).findById(1L);
        verify(simulacaoRepository).saveAll(anyList());
    }

    @Test
    void deveCadastrarSimulacoes() {
        List<SimulacaoRequest> requests = IntStream.range(0, 3)
            .mapToObj(i -> new SimulacaoRequest(
                new BigDecimal("30000").add(new BigDecimal(i)),
                new BigDecimal("50000.50"),
                420,
                new BigDecimal("2.5")
            )).toList();

        Cliente cliente = new Cliente();

        List<Simulacao> simulacoes = requests.stream()
            .map(request ->
                Simulacao.builder()
                    .cliente(cliente)
                    .valorSolicitado(request.valorSolicitado())
                    .valorGarantia(request.valorGarantia())
                    .quantidadeMeses(request.quantidadeMeses())
                    .taxaJurosMensal(request.taxaJurosMensal())
                    .build()
            ).toList();

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(cliente));

        when(simulacaoRepository.saveAll(anyList()))
            .thenReturn(simulacoes);

        List<SimulacaoResponse> response = simulacaoService.save(1L, requests);

        assertEquals(3, response.size());
        assertEquals(new BigDecimal("30000"), response.get(0).valorSolicitado());
        assertEquals(new BigDecimal("30001"), response.get(1).valorSolicitado());
        assertEquals(new BigDecimal("30002"), response.get(2).valorSolicitado());

        verify(clienteRepository).findById(1L);
        verify(simulacaoRepository).saveAll(simulacoesCaptor.capture());

        List<List<Simulacao>> capturedValues = simulacoesCaptor.getAllValues();
        assertEquals(1, capturedValues.size());

        List<Simulacao> simulacoesSalvas = capturedValues.getFirst();
        assertEquals(3, simulacoesSalvas.size());

        assertEquals(
            new BigDecimal("30000"),
            simulacoesSalvas.get(0).getValorSolicitado()
        );

        assertEquals(
            new BigDecimal("30001"),
            simulacoesSalvas.get(1).getValorSolicitado()
        );

        assertEquals(
            new BigDecimal("30002"),
            simulacoesSalvas.get(2).getValorSolicitado()
        );
    }
}
