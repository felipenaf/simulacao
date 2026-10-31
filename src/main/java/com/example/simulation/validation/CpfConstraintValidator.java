package com.example.simulation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfConstraintValidator implements ConstraintValidator<ValidCPF, String> {

    @Override
    public boolean isValid(
        String cpf,
        ConstraintValidatorContext context
    ) {
        // Campos obrigatórios são tratados por @NotBlank.
        if (cpf == null || cpf.isBlank()) {
            return true;
        }

        return CpfValidator.isValid(cpf);
    }
}