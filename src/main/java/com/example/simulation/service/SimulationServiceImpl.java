package com.example.simulation.service;

import com.example.simulation.dto.SimulationRequest;
import com.example.simulation.dto.SimulationResponse;
import com.example.simulation.entity.Customer;
import com.example.simulation.entity.Simulation;
import com.example.simulation.exception.CustomerNaoEncontradoException;
import com.example.simulation.repository.CustomerRepository;
import com.example.simulation.repository.SimulationRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulationServiceImpl implements SimulationService {
    private final SimulationRepository simulationRepository;
    private final CustomerRepository customerRepository;

    public SimulationServiceImpl(
        SimulationRepository simulationRepository,
        CustomerRepository customerRepository
    ) {
        this.simulationRepository = simulationRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public List<SimulationResponse> save(
        Long customerId,
        List<SimulationRequest> requests
    ) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId não pode ser null");
        }

        if (requests == null) {
            throw new IllegalArgumentException("requests não pode ser null");
        }

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() ->
                new CustomerNaoEncontradoException("Customer não encontrado")
            );

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

        return simulationRepository
            .saveAll(simulations)
            .stream()
            .map(SimulationResponse::from)
            .toList();
    }

    @Override
    @Transactional
    public Page<SimulationResponse> findByCustomerId(
        Long customerId,
        Pageable pageable
    ) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId não pode ser null");
        }

        if (pageable == null) {
            throw new IllegalArgumentException("pageable não pode ser null");
        }

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() ->
                new CustomerNaoEncontradoException("Customer não encontrado")
            );

        return simulationRepository
            .findByCustomer(customer, pageable)
            .map(SimulationResponse::from);
    }

    // TODO
    //Crie um metodo que calcule a primeira parcela de uma simulação com as seguintes informações:
    //Data de vencimento: 1 mês após a data da simulação
    //Valor de amortização: valor solicitado ÷ quantidade de meses
    //Valor de juros: valor solicitado * taxa de juros
    //Valor total da parcela: valor de amortização + valor de juros
    //Saldo devedor: valor solicitado − valor de amortização
    public ParcelaSimulacao calculaParcela(Simulation simulation) {
        return null;
    }
}
