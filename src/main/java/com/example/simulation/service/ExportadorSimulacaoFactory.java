package com.example.simulation.service;

import com.example.simulation.enums.FormatoExportacao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExportadorSimulacaoFactory {
    private final Map<String, SimulationExporter> exportadores;

    public ExportadorSimulacaoFactory(
        List<SimulationExporter> exportadores
    ) {
        this.exportadores = exportadores.stream()
            .collect(Collectors.toMap(
                exportador -> exportador.format().toLowerCase(Locale.ROOT),
                // Function.identity() é a mesma coisa que exportador -> exportador
                Function.identity()
            ));
    }

    public Optional<SimulationExporter> buscar(FormatoExportacao formato) {
        if (formato == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
            exportadores.get(formato.name().toLowerCase(Locale.ROOT))
        );
    }
}