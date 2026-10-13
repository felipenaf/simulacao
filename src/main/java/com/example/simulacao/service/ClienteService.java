package com.example.simulacao.service;

import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ClienteResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClienteService {
    void save(ClienteRequest request);

    ClienteResponse findById(Long id);

    List<ClienteResponse> findAll(Pageable pageable);

    void update(Long id, ClienteRequest request);

    void delete(Long id);
}
