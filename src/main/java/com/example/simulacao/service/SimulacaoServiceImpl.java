package com.example.simulacao.service;

import com.example.simulacao.dto.SimulacaoRequest;
import com.example.simulacao.dto.SimulacaoResponse;
import com.example.simulacao.entity.Cliente;
import com.example.simulacao.entity.Simulacao;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.repository.ClienteRepository;
import com.example.simulacao.repository.SimulacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulacaoServiceImpl implements SimulacaoService {
    private final SimulacaoRepository simulacaoRepository;
    private final ClienteRepository clienteRepository;

    public SimulacaoServiceImpl(
        SimulacaoRepository simulacaoRepository,
        ClienteRepository clienteRepository
    ) {
        this.simulacaoRepository = simulacaoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public List<SimulacaoResponse> save(
        Long clienteId,
        List<SimulacaoRequest> requests
    ) {
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado")
            );

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

        return simulacaoRepository
            .saveAll(simulacoes)
            .stream()
            .map(SimulacaoResponse::from)
            .toList();
    }

    @Override
    @Transactional
    public Page<SimulacaoResponse> findByClienteId(
        Long clienteId,
        Pageable pageable
    ) {
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado")
            );

        return simulacaoRepository
            .findByCliente(cliente, pageable)
            .map(SimulacaoResponse::from);
    }
}
