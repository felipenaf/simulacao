package com.example.simulacao.service;

import com.example.simulacao.dto.SimulacaoRequest;
import com.example.simulacao.dto.SimulacaoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SimulacaoService {
    List<SimulacaoResponse> save(Long clienteId, List<SimulacaoRequest> request);

    Page<SimulacaoResponse> findByCustomerId(Long clienteId, Pageable pageable);
}
