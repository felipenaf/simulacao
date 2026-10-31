package com.example.simulation.dto;

import com.example.simulation.entity.State;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CustomerPatchRequest(
    @Size(min = 11, max = 11, message = "CPF deve ter 11 caracteres")
    String cpf,

    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String name,

    @Valid
    AddressRequest address
) {
    public CustomerPatchRequest {
        cpf = cpf == null ? null : cpf.replaceAll("[.-]", "").trim();
        name = name == null ? null : name.trim();
    }

    public record AddressRequest(
        @Size(max = 150, message = "Rua deve ter no máximo 150 caracteres")
        String street,

        @Positive(message = "Número deve ser positivo")
        Integer number,

        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String neighborhood,

        @Size(min = 8, max = 8, message = "CEP deve conter exatamente 8 caracteres")
        String postalCode,

        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String city,

        State state
    ) {
        public AddressRequest {
            street = street == null ? null : street.trim();
            neighborhood = neighborhood == null ? null : neighborhood.trim();
            postalCode = postalCode == null ? null : postalCode.replace("-", "").trim();
            city = city == null ? null : city.trim();
        }
    }
}