package com.example.simulation.service;

import com.example.simulation.dto.SimulationRequest;
import com.example.simulation.dto.SimulationResponse;
import com.example.simulation.entity.Customer;
import com.example.simulation.entity.Simulation;
import com.example.simulation.exception.CustomerNaoEncontradoException;
import com.example.simulation.repository.CustomerRepository;
import com.example.simulation.repository.SimulationRepository;
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
public class SimulationServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SimulationRepository simulationRepository;

    @InjectMocks
    private SimulationServiceImpl simulationService;

    @Captor
    private ArgumentCaptor<List<Simulation>> simulacoesCaptor;

    @Test
    void deveCadastrarSimulacao() {
        List<SimulationRequest> requests = IntStream.range(0, 1)
            .mapToObj(i -> new SimulationRequest(
                new BigDecimal("30000").add(new BigDecimal(i)),
                new BigDecimal("50000.50"),
                420,
                new BigDecimal("2.5")
            )).toList();

        Customer customer = new Customer();

        List<Simulation> simulations = requests.stream()
            .map(request ->
                Simulation.builder()
                    .customer(customer)
                    .valorSolicitado(request.valorSolicitado())
                    .valorGarantia(request.valorGarantia())
                    .quantidadeMeses(request.quantidadeMeses())
                    .taxaJurosMensal(request.taxaJurosMensal())
                    .build()
            ).toList();

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(simulationRepository.saveAll(anyList()))
            .thenReturn(simulations);

        List<SimulationResponse> response =
            simulationService.save(1L, requests);

        assertEquals(1, response.size());
        assertEquals(new BigDecimal("30000"), response.getFirst().valorSolicitado());

        verify(customerRepository).findById(1L);
        verify(simulationRepository).saveAll(anyList());
    }

    @Test
    void deveCadastrarSimulacoes() {
        List<SimulationRequest> requests = IntStream.range(0, 3)
            .mapToObj(i -> new SimulationRequest(
                new BigDecimal("30000").add(new BigDecimal(i)),
                new BigDecimal("50000.50"),
                420,
                new BigDecimal("2.5")
            )).toList();

        Customer customer = new Customer();

        List<Simulation> simulations = requests.stream()
            .map(request ->
                Simulation.builder()
                    .customer(customer)
                    .valorSolicitado(request.valorSolicitado())
                    .valorGarantia(request.valorGarantia())
                    .quantidadeMeses(request.quantidadeMeses())
                    .taxaJurosMensal(request.taxaJurosMensal())
                    .build()
            ).toList();

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(simulationRepository.saveAll(anyList()))
            .thenReturn(simulations);

        List<SimulationResponse> response = simulationService.save(1L, requests);

        assertEquals(3, response.size());
        assertEquals(new BigDecimal("30000"), response.get(0).valorSolicitado());
        assertEquals(new BigDecimal("30001"), response.get(1).valorSolicitado());
        assertEquals(new BigDecimal("30002"), response.get(2).valorSolicitado());

        verify(customerRepository).findById(1L);
        verify(simulationRepository).saveAll(simulacoesCaptor.capture());

        List<List<Simulation>> capturedValues = simulacoesCaptor.getAllValues();
        assertEquals(1, capturedValues.size());

        List<Simulation> simulacoesSalvas = capturedValues.getFirst();
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
        List<SimulationRequest> requests = IntStream.range(0, 3)
            .mapToObj(i -> new SimulationRequest(
                new BigDecimal("30000").add(new BigDecimal(i)),
                new BigDecimal("50000.50"),
                420,
                new BigDecimal("2.5")
            )).toList();

        List<Simulation> simulations = requests.stream()
            .map(request ->
                Simulation.builder()
                    .customer(customer)
                    .valorSolicitado(request.valorSolicitado())
                    .valorGarantia(request.valorGarantia())
                    .quantidadeMeses(request.quantidadeMeses())
                    .taxaJurosMensal(request.taxaJurosMensal())
                    .build()
            ).toList();

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(simulationRepository.saveAll(anyList()))
            .thenReturn(simulations);

        List<SimulationResponse> response = simulationService.save(1L, requests);

        assertNotNull(response);
        assertEquals(3, response.size());
        assertEquals(new BigDecimal("30000"), response.get(0).valorSolicitado());
        assertEquals(new BigDecimal("30001"), response.get(1).valorSolicitado());
        assertEquals(new BigDecimal("30002"), response.get(2).valorSolicitado());

        verify(customerRepository).findById(1L);
        verify(simulationRepository).saveAll(anyList());
    }

    @Test
    void deveLancarCustomerNaoEncontradoExceptionAoCadastrarSimulacao() {
        List<SimulationRequest> requests = IntStream.range(0, 1)
            .mapToObj(i -> new SimulationRequest(
                new BigDecimal("30000").add(new BigDecimal(i)),
                new BigDecimal("50000.50"),
                420,
                new BigDecimal("2.5")
            )).toList();

        when(customerRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            CustomerNaoEncontradoException.class,
            () -> simulationService.save(1L, requests)
        );

        verify(simulationRepository, never()).saveAll(anyList());
    }

    @Test
    void deveLancarCustomerNaoEncontradoExceptionAoCadastrarSimulacaoComRequestsVazio() {
        when(customerRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            CustomerNaoEncontradoException.class,
            () -> simulationService.save(1L, List.of())
        );

        verify(simulationRepository, never()).saveAll(anyList());
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoCadastrarSimulacaoComRequestsNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> simulationService.save(1L, null)
        );
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoCadastrarSimulacaoComCustomerIdNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> simulationService.save(null, List.of())
        );
    }

    @Test
    void deveConsultarSimulacoes() {
        Customer customer = new Customer();
        List<Simulation> simulations = IntStream.range(0, 10)
            .mapToObj(i ->
                Simulation.builder()
                    .customer(customer)
                    .valorSolicitado(new BigDecimal("30000").add(new BigDecimal(i)))
                    .valorGarantia(new BigDecimal("50000.50"))
                    .quantidadeMeses(420)
                    .taxaJurosMensal(new BigDecimal("2.5"))
                    .build()
            ).toList();

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        Pageable pageable = PageRequest.of(0, 2);

        when(simulationRepository.findByCustomer(eq(customer), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(
                simulations.subList(0, 2),
                pageable,
                simulations.size()
            ));

        Page<SimulationResponse> response = simulationService.findByCustomerId(
            1L,
            pageable
        );

        assertEquals(5, response.getTotalPages());
        assertEquals(new BigDecimal("30000"), response.getContent().get(0).valorSolicitado());
        assertEquals(new BigDecimal("30001"), response.getContent().get(1).valorSolicitado());

        verify(customerRepository).findById(1L);
        verify(simulationRepository).findByCustomer(customer, pageable);
    }

    @Test
    void deveRetornarListaVaziaAoConsultarSimulacoes() {
        Customer customer = new Customer();

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        Pageable pageable = PageRequest.of(0, 1);

        when(simulationRepository.findByCustomer(eq(customer), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(
                List.of(),
                pageable,
                0
            ));

        Page<SimulationResponse> response = simulationService.findByCustomerId(
            1L,
            pageable
        );

        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getTotalElements());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertTrue(response.isEmpty());

        verify(customerRepository).findById(1L);
        verify(simulationRepository).findByCustomer(customer, pageable);
    }

    @Test
    void deveLancarCustomerNaoEncontradoExceptionAoConsultarSimulacao() {
        assertThrows(
            CustomerNaoEncontradoException.class,
            () -> simulationService.findByCustomerId(1L, PageRequest.of(0, 10))
        );
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoConsultarSimulacaoComPageableNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> simulationService.findByCustomerId(1L, null)
        );
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoConsultarSimulacaoComCustomerIdNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> simulationService.findByCustomerId(null, PageRequest.of(0, 10))
        );
    }
}
