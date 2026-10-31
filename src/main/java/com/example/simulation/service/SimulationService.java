package com.example.simulation.service;

import com.example.simulation.dto.SimulationRequest;
import com.example.simulation.dto.SimulationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SimulationService {
    List<SimulationResponse> save(Long customerId, List<SimulationRequest> request);

    Page<SimulationResponse> findByCustomerId(Long customerId, Pageable pageable);
}
