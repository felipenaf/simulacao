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
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

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
        Cliente cliente,
        BigDecimal valorSolicitado,
        BigDecimal valorGarantia,
        Integer quantidadeMeses,
        BigDecimal taxaJurosMensal
    ) {
        this.cliente = cliente;
        this.valorSolicitado = valorSolicitado;
        this.valorGarantia = valorGarantia;
        this.quantidadeMeses = quantidadeMeses;
        this.taxaJurosMensal = taxaJurosMensal;
    }
}