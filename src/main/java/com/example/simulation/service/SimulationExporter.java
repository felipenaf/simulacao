package com.example.simulation.service;

import com.example.simulation.dto.SimulationResponse;

import java.io.IOException;
import java.util.List;

public interface SimulationExporter {
    String format();

    String contentType();

    String filename(Long customerId);

    byte[] export(List<SimulationResponse> simulations) throws IOException;
}