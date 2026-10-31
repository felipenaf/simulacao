package com.example.simulation.service;

import com.example.simulation.dto.SimulationResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ExportadorSimulacaoTxt implements ExportadorSimulacao{
    private final ExportadorSimulacaoCsv exportadorSimulacaoCsv;

    public ExportadorSimulacaoTxt(ExportadorSimulacaoCsv exportadorSimulacaoCsv) {
        this.exportadorSimulacaoCsv = exportadorSimulacaoCsv;
    }

    @Override
    public String formato() {
        return "txt";
    }

    @Override
    public String contentType() {
        return "text/plain;charset=UTF-8";
    }

    @Override
    public String nomeArquivo(Long customerId) {
        return "simulacoes_customer_" + customerId + "." +  this.formato();
    }

    @Override
    public byte[] exportar(
        List<SimulationResponse> simulations
    ) throws IOException {
        return exportadorSimulacaoCsv.exportar(simulations);
    }
}