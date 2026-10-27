package com.example.simulacao.dto;

import com.example.simulacao.entity.Customer;
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

    public static ClienteResponse from(Customer customer) {
        ClienteResponse.EnderecoResponse enderecoResponse =
            new ClienteResponse.EnderecoResponse(
                customer.getEndereco().getRua(),
                customer.getEndereco().getNumero(),
                customer.getEndereco().getBairro(),
                customer.getEndereco().getCep(),
                customer.getEndereco().getCidade(),
                customer.getEndereco().getEstado()
            );

        return new ClienteResponse(
            customer.getCpf(),
            customer.getNome(),
            enderecoResponse
        );
    }
}