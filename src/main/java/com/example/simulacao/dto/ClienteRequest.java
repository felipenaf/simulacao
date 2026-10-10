package com.example.simulacao.dto;

import com.example.simulacao.entity.Estado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ClienteRequest(
    @NotBlank(message = "Campo CPF inválido")
    String cpf,

    @NotBlank(message = "Campo Nome inválido")
    String nome,

    @NotNull(message = "Campo Endereço inválido")
    @Valid
    EnderecoRequest endereco
) {
    public record EnderecoRequest(
        @NotBlank(message = "Campo rua inválido")
        String rua,

        @NotNull(message = "Campo número inválido")
        @Positive(message = "Campo número deve ser positivo")
        Integer numero,

        @NotBlank(message = "Campo bairro inválido")
        String bairro,

        @NotBlank(message = "Campo cep inválido")
        String cep,

        @NotBlank(message = "Campo cidade inválido")
        String cidade,

        @NotNull(message = "Campo estado inválido")
        Estado estado
    ) {}
}