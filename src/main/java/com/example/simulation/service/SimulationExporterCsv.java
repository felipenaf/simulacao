package com.example.simulation.service;

import com.example.simulation.dto.SimulationResponse;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class SimulationExporterCsv implements SimulationExporter {
    private final CsvMapper csvMapper = CsvMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    @Override
    public String format() {
        return "csv";
    }

    @Override
    public String contentType() {
        return "text/csv;charset=UTF-8";
    }

    @Override
    public String filename(Long customerId) {
        return "simulacoes_customer_" + customerId + "." +  this.format();
    }

    @Override
    public byte[] export(
        List<SimulationResponse> simulations
    ) throws IOException {
        CsvSchema schema = csvMapper
            .schemaFor(SimulationResponse.class)
            .withHeader()
            .withColumnSeparator(';');

        String csv = csvMapper
            .writer(schema)
            .writeValueAsString(simulations);

        return csv.getBytes(StandardCharsets.UTF_8);
    }
}