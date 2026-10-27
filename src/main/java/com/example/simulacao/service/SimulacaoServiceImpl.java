package com.example.simulacao.service;

import com.example.simulacao.dto.SimulacaoRequest;
import com.example.simulacao.dto.SimulacaoResponse;
import com.example.simulacao.entity.Customer;
import com.example.simulacao.entity.Simulacao;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.repository.CustomerRepository;
import com.example.simulacao.repository.SimulacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulacaoServiceImpl implements SimulacaoService {
    private final SimulacaoRepository simulacaoRepository;
    private final CustomerRepository customerRepository;

    public SimulacaoServiceImpl(
        SimulacaoRepository simulacaoRepository,
        CustomerRepository customerRepository
    ) {
        this.simulacaoRepository = simulacaoRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public List<SimulacaoResponse> save(
        Long clienteId,
        List<SimulacaoRequest> requests
    ) {
        if (clienteId == null) {
            throw new IllegalArgumentException("clienteId não pode ser null");
        }

        if (requests == null) {
            throw new IllegalArgumentException("requests não pode ser null");
        }

        Customer customer = customerRepository.findById(clienteId)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado")
            );

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

        return simulacaoRepository
            .saveAll(simulacoes)
            .stream()
            .map(SimulacaoResponse::from)
            .toList();
    }

    @Override
    @Transactional
    public Page<SimulacaoResponse> findByCustomerId(
        Long clienteId,
        Pageable pageable
    ) {
        if (clienteId == null) {
            throw new IllegalArgumentException("clienteId não pode ser null");
        }

        if (pageable == null) {
            throw new IllegalArgumentException("pageable não pode ser null");
        }

        Customer customer = customerRepository.findById(clienteId)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado")
            );

        return simulacaoRepository
            .findByCustomer(customer, pageable)
            .map(SimulacaoResponse::from);
    }

    // TODO
    //Crie um metodo que calcule a primeira parcela de uma simulação com as seguintes informações:
    //Data de vencimento: 1 mês após a data da simulação
    //Valor de amortização: valor solicitado ÷ quantidade de meses
    //Valor de juros: valor solicitado * taxa de juros
    //Valor total da parcela: valor de amortização + valor de juros
    //Saldo devedor: valor solicitado − valor de amortização
    public ParcelaSimulacao calculaParcela(Simulacao simulacao) {
        return null;
    }
}
