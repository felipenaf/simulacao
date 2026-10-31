package com.example.simulation.service;

import com.example.simulation.dto.SimulationResponse;

import java.io.IOException;
import java.util.List;

public interface ExportadorSimulacao {
    String formato();

    String contentType();

    String nomeArquivo(Long customerId);

    byte[] exportar(List<SimulationResponse> simulations) throws IOException;
}