package com.example.simulacao.dto;

import com.example.simulacao.entity.Cliente;
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

    public static ClienteResponse from(Cliente cliente) {
        ClienteResponse.EnderecoResponse enderecoResponse =
            new ClienteResponse.EnderecoResponse(
                cliente.getEndereco().getRua(),
                cliente.getEndereco().getNumero(),
                cliente.getEndereco().getBairro(),
                cliente.getEndereco().getCep(),
                cliente.getEndereco().getCidade(),
                cliente.getEndereco().getEstado()
            );

        return new ClienteResponse(
            cliente.getCpf(),
            cliente.getNome(),
            enderecoResponse
        );
    }
}