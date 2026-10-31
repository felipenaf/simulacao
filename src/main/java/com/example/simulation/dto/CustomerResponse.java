package com.example.simulation.dto;

import com.example.simulation.entity.Customer;
import com.example.simulation.entity.State;

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
        State state
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
                customer.getAddress().getState()
            )
        );
    }
}