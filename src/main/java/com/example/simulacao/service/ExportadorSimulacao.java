package com.example.simulacao.service;

import com.example.simulacao.dto.SimulacaoResponse;

import java.io.IOException;
import java.util.List;

public interface ExportadorSimulacao {
    String formato();

    String contentType();

    byte[] exportar(List<SimulacaoResponse> simulacoes) throws IOException;
}