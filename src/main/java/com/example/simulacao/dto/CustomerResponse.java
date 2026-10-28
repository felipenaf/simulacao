package com.example.simulacao.dto;

import com.example.simulacao.entity.Customer;
import com.example.simulacao.entity.Estado;

public record CustomerResponse(
    String cpf,
    String name,
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

    public static CustomerResponse from(Customer customer) {
        CustomerResponse.EnderecoResponse enderecoResponse =
            new CustomerResponse.EnderecoResponse(
                customer.getEndereco().getRua(),
                customer.getEndereco().getNumero(),
                customer.getEndereco().getBairro(),
                customer.getEndereco().getCep(),
                customer.getEndereco().getCidade(),
                customer.getEndereco().getEstado()
            );

        return new CustomerResponse(
            customer.getCpf(),
            customer.getName(),
            enderecoResponse
        );
    }
}