package com.example.simulation.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record SimulationRequest(
    @NotNull(message = "Valor solicitado é obrigatório")
    @DecimalMin(value = "1000.00", message = "Valor solicitado não pode ser menor que 1000.00")
    @DecimalMax(value = "999999999.99", message = "Valor solicitado não pode ultrapassar 999999999.99")
    @Digits(integer = 11, fraction = 2, message = "Valor solicitado deve ter no máximo 2 casas decimais")
    BigDecimal valorSolicitado,

    @NotNull(message = "Valor da garantia é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor da garantia não pode ser negativa")
    @DecimalMax(value = "999999999.99", message = "Valor da garantia não pode ultrapassar 999999999.99")
    @Digits(integer = 11, fraction = 2, message = "Valor da garantia deve ter no máximo 2 casas decimais")
    BigDecimal valorGarantia,

    @NotNull(message = "Quantidade de meses é obrigatória")
    @Positive(message = "Quantidade de meses ser positiva")
    @Max(value = 420, message = "Quantidade de meses não pode ultrapassar 420") // 35 anos
    Integer quantidadeMeses,

    @NotNull(message = "Taxa de juros mensal é obrigatória")
    @DecimalMin(value = "0.00", message = "Taxa de juros mensal não pode ser negativa")
    @DecimalMax(value = "99.99", message = "Taxa de juros mensal não pode ultrapassar 99.99")
    @Digits(integer = 4, fraction = 2, message = "Taxa de juros mensal deve ter no máximo 2 casas decimais")
    BigDecimal taxaJurosMensal
) {
}