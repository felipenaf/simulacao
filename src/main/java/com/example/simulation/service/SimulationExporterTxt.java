package com.example.simulation.service;

import com.example.simulation.dto.SimulationResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class SimulationExporterTxt implements SimulationExporter {
    private final SimulationExporterCsv simulationExporterCsv;

    public SimulationExporterTxt(SimulationExporterCsv simulationExporterCsv) {
        this.simulationExporterCsv = simulationExporterCsv;
    }

    @Override
    public String format() {
        return "txt";
    }

    @Override
    public String contentType() {
        return "text/plain;charset=UTF-8";
    }

    @Override
    public String filename(Long customerId) {
        return "customer_simulations_" + customerId + "." +  this.format();
    }

    @Override
    public byte[] export(
        List<SimulationResponse> simulations
    ) throws IOException {
        return simulationExporterCsv.export(simulations);
    }
}