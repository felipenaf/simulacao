package com.example.simulacao.dto;

import com.example.simulacao.entity.Estado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CustomerPatchRequest(
    @Size(min = 11, max = 11, message = "CPF deve ter 11 caracteres")
    String cpf,

    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String name,

    @Valid
    EnderecoRequest endereco
) {
    public CustomerPatchRequest {
        cpf = cpf == null ? null : cpf.replaceAll("[.-]", "").trim();
        name = name == null ? null : name.trim();
    }

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
    ) {
        public EnderecoRequest {
            rua = rua == null ? null : rua.trim();
            bairro = bairro == null ? null : bairro.trim();
            cep = cep == null ? null : cep.replace("-", "").trim();
            cidade = cidade == null ? null : cidade.trim();
        }
    }
}