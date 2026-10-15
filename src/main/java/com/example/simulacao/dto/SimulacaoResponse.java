package com.example.simulacao.dto;

import com.example.simulacao.entity.Simulacao;
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
public record SimulacaoResponse(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime dataHora,
    BigDecimal valorSolicitado,
    BigDecimal valorGarantia,
    Integer quantidadeMeses,
    BigDecimal taxaJurosMensal
) {
    public static SimulacaoResponse from(Simulacao simulacao) {
        return new SimulacaoResponse(
            simulacao.getDataHora(),
            simulacao.getValorSolicitado(),
            simulacao.getValorGarantia(),
            simulacao.getQuantidadeMeses(),
            simulacao.getTaxaJurosMensal()
        );
    }
}