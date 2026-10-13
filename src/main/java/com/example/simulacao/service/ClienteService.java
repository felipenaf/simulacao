package com.example.simulacao.service;

import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ClienteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {
    void save(ClienteRequest request);

    ClienteResponse findById(Long id);

    Page<ClienteResponse> findAll(Pageable pageable);

    void update(Long id, ClienteRequest request);

    void delete(Long id);
}
