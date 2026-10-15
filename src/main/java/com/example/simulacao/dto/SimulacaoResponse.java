package com.example.simulacao.dto;

import com.example.simulacao.entity.Simulacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SimulacaoResponse(
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