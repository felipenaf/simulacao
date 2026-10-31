package com.example.simulation.service;

import com.example.simulation.enums.ExportFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SimulationExporterFactory {
    private final Map<String, SimulationExporter> exporters;

    public SimulationExporterFactory(
        List<SimulationExporter> exporters
    ) {
        this.exporters = exporters.stream()
            .collect(Collectors.toMap(
                exporter -> exporter.format().toLowerCase(Locale.ROOT),
                // Function.identity() is the same that exporter -> exporter
                Function.identity()
            ));
    }

    public Optional<SimulationExporter> find(ExportFormat format) {
        if (format == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
            exporters.get(format.name().toLowerCase(Locale.ROOT))
        );
    }
}