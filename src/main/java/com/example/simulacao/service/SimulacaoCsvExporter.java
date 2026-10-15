package com.example.simulacao.service;

import com.example.simulacao.dto.SimulacaoResponse;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class SimulacaoCsvExporter {
    private final CsvMapper csvMapper = CsvMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    public byte[] exportar(List<SimulacaoResponse> simulacoes) throws IOException {
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