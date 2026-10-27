package com.example.simulacao.service;

import com.example.simulacao.dto.SimulacaoResponse;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ExportadorSimulacaoCsv implements ExportadorSimulacao{
    private final CsvMapper csvMapper = CsvMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    @Override
    public String formato() {
        return "csv";
    }

    @Override
    public String contentType() {
        return "text/csv;charset=UTF-8";
    }

    @Override
    public String nomeArquivo(Long customerId) {
        return "simulacoes_customer_" + customerId + "." +  this.formato();
    }

    @Override
    public byte[] exportar(
        List<SimulacaoResponse> simulacoes
    ) throws IOException {
        CsvSchema schema = csvMapper
            .schemaFor(SimulacaoResponse.class)
            .withHeader()
            .withColumnSeparator(';');

        String csv = csvMapper
            .writer(schema)
            .writeValueAsString(simulacoes);

        return csv.getBytes(StandardCharsets.UTF_8);
    }
}