package com.example.simulation.dto;

import com.example.simulation.entity.Customer;
import com.example.simulation.entity.State;

public record CustomerResponse(
    String cpf,
    String name,
    AddressResponse address
) {
    public record AddressResponse(
        String street,
        Integer number,
        String neighborhood,
        String postalCode,
        String city,
        State state
    ) {}

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
            customer.getCpf(),
            customer.getName(),
            new AddressResponse(
                customer.getAddress().getStreet(),
                customer.getAddress().getNumber(),
                customer.getAddress().getNeighborhood(),
                customer.getAddress().getPostalCode(),
                customer.getAddress().getCity(),
                customer.getAddress().getState()
            )
        );
    }
}