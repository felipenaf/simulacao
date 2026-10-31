package com.example.simulation.dto;

import com.example.simulation.entity.State;
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
        String street,

        @NotNull(message = "Número inválido")
        @Positive(message = "Número deve ser positivo")
        Integer number,

        @NotBlank(message = "Bairro inválido")
        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String neighborhood,

        @NotBlank(message = "CEP inválido")
        @Size(min = 8, max = 8, message = "CEP deve conter exatamente 8 caracteres")
        String postalCode,

        @NotBlank(message = "Cidade inválida")
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String city,

        @NotNull(message = "Estado inválido")
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