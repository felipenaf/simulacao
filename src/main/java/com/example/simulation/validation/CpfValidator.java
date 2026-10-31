package com.example.simulation.validation;

public class CpfValidator {
    public static boolean isValid(String cpf) {
        if (cpf == null) {
            return false;
        }

        return cpf.replaceAll("[.\\-]", "").length() == 11;
    }
}