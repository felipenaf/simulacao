package com.example.simulacao.dto;

import com.example.simulacao.entity.Estado;
import com.example.simulacao.validation.ValidCPF;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClientePatchRequest(
    @ValidCPF
    String cpf,

    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String nome,

    @Valid
    EnderecoRequest endereco
) {
    public record EnderecoRequest(
        @Size(max = 150, message = "Rua deve ter no máximo 150 caracteres")
        String rua,

        @Positive(message = "Número deve ser positivo")
        Integer numero,

        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String bairro,

        @Size(min = 8, max = 8, message = "CEP deve conter exatamente 8 caracteres")
        String cep,

        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String cidade,

        Estado estado
    ) {}
}