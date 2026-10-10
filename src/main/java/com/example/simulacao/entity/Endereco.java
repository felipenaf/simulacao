package com.example.simulacao.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@Entity
@ToString
@Table(name = "endereco")
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rua;

    private Integer numero;

    private String bairro;

    private String cep;

    private String cidade;

    @Enumerated(EnumType.STRING)
    private Estado estado;
}