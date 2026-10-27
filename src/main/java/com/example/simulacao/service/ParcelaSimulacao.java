package com.example.simulacao.service;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
public class ParcelaSimulacao {
    private BigDecimal valorAmortizacao;
    private LocalDate dataVencimento;
    private BigDecimal valorJuros;
    private BigDecimal valorTotal;
    private BigDecimal saldoDevedor;
}