package com.example.simulacao.dto;

import com.example.simulacao.entity.State;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
    @NotBlank(message = "CPF inválido")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 caracteres")
    String cpf,

    @NotBlank(message = "Nome inválido")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String name,

    @NotNull(message = "Endereço inválido")
    @Valid
    AddressRequest address
) {
    public CustomerRequest {
        cpf = cpf == null ? null : cpf.replaceAll("[.-]", "").trim();
        name = name == null ? null : name.trim();
    }

    public record AddressRequest(
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
        State state
    ) {
        public AddressRequest {
            rua = rua == null ? null : rua.trim();
            bairro = bairro == null ? null : bairro.trim();
            cep = cep == null ? null : cep.replace("-", "").trim();
            cidade = cidade == null ? null : cidade.trim();
        }
    }
}