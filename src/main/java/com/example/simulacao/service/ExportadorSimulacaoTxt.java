package com.example.simulacao.service;

import com.example.simulacao.dto.SimulacaoResponse;
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

    public byte[] exportar(
        List<SimulacaoResponse> simulacoes
    ) throws IOException {
        return exportadorSimulacaoCsv.exportar(simulacoes);
    }
}