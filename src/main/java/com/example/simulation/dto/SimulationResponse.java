package com.example.simulation.dto;

import com.example.simulation.entity.Simulation;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonPropertyOrder({
    "dataHora",
    "valorSolicitado",
    "valorGarantia",
    "quantidadeMeses",
    "taxaJurosMensal"
})
public record SimulationResponse(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime dataHora,
    BigDecimal valorSolicitado,
    BigDecimal valorGarantia,
    Integer quantidadeMeses,
    BigDecimal taxaJurosMensal
) {
    public static SimulationResponse from(Simulation simulation) {
        return new SimulationResponse(
            simulation.getDateTime(),
            simulation.getAmount(),
            simulation.getAssuranceAmount(),
            simulation.getMonths(),
            simulation.getInterestRate()
        );
    }
}