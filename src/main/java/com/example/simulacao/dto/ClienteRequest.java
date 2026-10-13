package com.example.simulacao.dto;

import com.example.simulacao.entity.Estado;
import com.example.simulacao.validation.ValidCPF;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
    @NotBlank(message = "CPF inválido")
    @ValidCPF
    String cpf,

    @NotBlank(message = "Nome inválido")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String nome,

    @NotNull(message = "Endereço inválido")
    @Valid
    EnderecoRequest endereco
) {
    public record EnderecoRequest(
        @NotBlank(message = "Rua inválida")
        @Size(max = 150, message = "Rua deve ter no máximo 150 caracteres")
        String rua,

        @NotNull(message = "Número inválido")
        @Positive(message = "Número deve ser positivo")
        Integer numero,

        @NotBlank(message = "Bairro inválido")
        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String bairro,

        @NotBlank(message = "CEP inválido")
        @Size(min = 8, max = 8, message = "CEP deve conter exatamente 8 caracteres")
        String cep,

        @NotBlank(message = "Cidade inválida")
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String cidade,

        @NotNull(message = "Estado inválido")
        Estado estado
    ) {}
}