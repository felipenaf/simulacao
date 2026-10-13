package com.example.simulacao.dto;

import com.example.simulacao.entity.Estado;

public record ClienteResponse(
    String cpf,
    String nome,
    EnderecoResponse endereco
) {
    public record EnderecoResponse(
        String rua,
        Integer numero,
        String bairro,
        String cep,
        String cidade,
        Estado estado
    ) {}
}