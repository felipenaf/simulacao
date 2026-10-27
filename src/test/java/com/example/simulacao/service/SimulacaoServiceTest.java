package com.example.simulacao.service;

import com.example.simulacao.dto.SimulacaoRequest;
import com.example.simulacao.dto.SimulacaoResponse;
import com.example.simulacao.entity.Customer;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SimulacaoServiceTest {
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
        List<SimulacaoRequest> requests = IntStream.range(0, 1)
            .mapToObj(i -> new SimulacaoRequest(
                new BigDecimal("30000").add(new BigDecimal(i)),
                new BigDecimal("50000.50"),
                420,
                new BigDecimal("2.5")
            )).toList();

        Customer customer = new Customer();

        List<Simulacao> simulacoes = requests.stream()
            .map(request ->
                Simulacao.builder()
                    .customer(customer)
                    .valorSolicitado(request.valorSolicitado())
                    .valorGarantia(request.valorGarantia())
                    .quantidadeMeses(request.quantidadeMeses())
                    .taxaJurosMensal(request.taxaJurosMensal())
                    .build()
            ).toList();

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(simulacaoRepository.saveAll(anyList()))
            .thenReturn(simulacoes);

        List<SimulacaoResponse> response =
            simulacaoService.save(1L, requests);

        assertEquals(1, response.size());
        assertEquals(new BigDecimal("30000"), response.getFirst().valorSolicitado());

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

        Customer customer = new Customer();

        List<Simulacao> simulacoes = requests.stream()
            .map(request ->
                Simulacao.builder()
                    .customer(customer)
                    .valorSolicitado(request.valorSolicitado())
                    .valorGarantia(request.valorGarantia())
                    .quantidadeMeses(request.quantidadeMeses())
                    .taxaJurosMensal(request.taxaJurosMensal())
                    .build()
            ).toList();

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(customer));

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

    @Test
    void deveCadastrarSimulacoesSimples() {
        Customer customer = new Customer();
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
                    .customer(customer)
                    .valorSolicitado(request.valorSolicitado())
                    .valorGarantia(request.valorGarantia())
                    .quantidadeMeses(request.quantidadeMeses())
                    .taxaJurosMensal(request.taxaJurosMensal())
                    .build()
            ).toList();

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(simulacaoRepository.saveAll(anyList()))
            .thenReturn(simulacoes);

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
    void deveLancarClienteNaoEncontradoExceptionAoCadastrarSimulacao() {
        List<SimulacaoRequest> requests = IntStream.range(0, 1)
            .mapToObj(i -> new SimulacaoRequest(
                new BigDecimal("30000").add(new BigDecimal(i)),
                new BigDecimal("50000.50"),
                420,
                new BigDecimal("2.5")
            )).toList();

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            ClienteNaoEncontradoException.class,
            () -> simulacaoService.save(1L, requests)
        );

        verify(simulacaoRepository, never()).saveAll(anyList());
    }

    @Test
    void deveLancarClienteNaoEncontradoExceptionAoCadastrarSimulacaoComRequestsVazio() {
        when(clienteRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            ClienteNaoEncontradoException.class,
            () -> simulacaoService.save(1L, List.of())
        );

        verify(simulacaoRepository, never()).saveAll(anyList());
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoCadastrarSimulacaoComRequestsNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> simulacaoService.save(1L, null)
        );
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoCadastrarSimulacaoComClienteIdNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> simulacaoService.save(null, List.of())
        );
    }

    @Test
    void deveConsultarSimulacoes() {
        Customer customer = new Customer();
        List<Simulacao> simulacoes = IntStream.range(0, 10)
            .mapToObj(i ->
                Simulacao.builder()
                    .customer(customer)
                    .valorSolicitado(new BigDecimal("30000").add(new BigDecimal(i)))
                    .valorGarantia(new BigDecimal("50000.50"))
                    .quantidadeMeses(420)
                    .taxaJurosMensal(new BigDecimal("2.5"))
                    .build()
            ).toList();

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        Pageable pageable = PageRequest.of(0, 2);

        when(simulacaoRepository.findByCustomer(eq(customer), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(
                simulacoes.subList(0, 2),
                pageable,
                simulacoes.size()
            ));

        Page<SimulacaoResponse> response = simulacaoService.findByCustomerId(
            1L,
            pageable
        );

        assertEquals(5, response.getTotalPages());
        assertEquals(new BigDecimal("30000"), response.getContent().get(0).valorSolicitado());
        assertEquals(new BigDecimal("30001"), response.getContent().get(1).valorSolicitado());

        verify(clienteRepository).findById(1L);
        verify(simulacaoRepository).findByCustomer(customer, pageable);
    }

    @Test
    void deveRetornarListaVaziaAoConsultarSimulacoes() {
        Customer customer = new Customer();

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        Pageable pageable = PageRequest.of(0, 1);

        when(simulacaoRepository.findByCustomer(eq(customer), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(
                List.of(),
                pageable,
                0
            ));

        Page<SimulacaoResponse> response = simulacaoService.findByCustomerId(
            1L,
            pageable
        );

        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getTotalElements());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertTrue(response.isEmpty());

        verify(clienteRepository).findById(1L);
        verify(simulacaoRepository).findByCustomer(customer, pageable);
    }

    @Test
    void deveLancarClienteNaoEncontradoExceptionAoConsultarSimulacao() {
        assertThrows(
            ClienteNaoEncontradoException.class,
            () -> simulacaoService.findByCustomerId(1L, PageRequest.of(0, 10))
        );
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoConsultarSimulacaoComPageableNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> simulacaoService.findByCustomerId(1L, null)
        );
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoConsultarSimulacaoComClienteIdNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> simulacaoService.findByCustomerId(null, PageRequest.of(0, 10))
        );
    }
}
