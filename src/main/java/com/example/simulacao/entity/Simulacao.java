package com.example.simulacao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "simulacao")
public class Simulacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorSolicitado;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorGarantia;

    @Column(nullable = false)
    private Integer quantidadeMeses;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal taxaJurosMensal;
}