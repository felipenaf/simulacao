package com.example.simulacao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "simulacao")
public class Simulacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, precision = 11, scale = 2)
    private BigDecimal valorSolicitado;

    @Column(nullable = false, precision = 11, scale = 2)
    private BigDecimal valorGarantia;

    @Column(nullable = false)
    private Integer quantidadeMeses;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal taxaJurosMensal;

    @PrePersist
    protected void prePersist() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }

    @Builder
    public Simulacao (
        Customer customer,
        BigDecimal valorSolicitado,
        BigDecimal valorGarantia,
        Integer quantidadeMeses,
        BigDecimal taxaJurosMensal
    ) {
        this.customer = customer;
        this.valorSolicitado = valorSolicitado;
        this.valorGarantia = valorGarantia;
        this.quantidadeMeses = quantidadeMeses;
        this.taxaJurosMensal = taxaJurosMensal;
    }
}