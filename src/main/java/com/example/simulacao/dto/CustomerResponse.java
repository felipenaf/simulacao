package com.example.simulacao.dto;

import com.example.simulacao.entity.Customer;
import com.example.simulacao.entity.Estado;

public record CustomerResponse(
    String cpf,
    String name,
    AddressResponse address
) {
    public record AddressResponse(
        String rua,
        Integer numero,
        String bairro,
        String cep,
        String cidade,
        Estado estado
    ) {}

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
            customer.getCpf(),
            customer.getName(),
            new AddressResponse(
                customer.getAddress().getRua(),
                customer.getAddress().getNumero(),
                customer.getAddress().getBairro(),
                customer.getAddress().getCep(),
                customer.getAddress().getCidade(),
                customer.getAddress().getEstado()
            )
        );
    }
}