package com.example.simulacao.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientePatchRequestTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldRemoveDotsAndHyphensFromCpf() {
        var request = new ClientePatchRequest(
            "123.456.789-03",
            null,
            null
        );

        assertEquals("12345678903", request.cpf());
    }

    @Test
    void shouldKeepCpfWithoutFormatting() {
        var request = new ClientePatchRequest(
            "12345678903",
            null,
            null
        );

        assertEquals("12345678903", request.cpf());
    }

    @Test
    void shouldPreserveNullCpf() {
        var request = new ClientePatchRequest(
            null,
            null,
            null
        );

        assertNull(request.cpf());
    }

    @Test
    void shouldTrimName() {
        var request = new ClientePatchRequest(
            null,
            "  Felipe Ferreira  ",
            null
        );

        assertEquals("Felipe Ferreira", request.nome());
    }

    @Test
    void shouldPreserveNullName() {
        var request = new ClientePatchRequest(
            null,
            null,
            null
        );

        assertNull(request.nome());
    }

    @Test
    void shouldTrimAddressFields() {
        var endereco = new ClientePatchRequest.EnderecoRequest(
            "  Rua das Flores  ",
            null,
            "  Centro  ",
            " 13330000 ",
            " Indaiatuba ",
            null
        );

        assertEquals("Rua das Flores", endereco.rua());
        assertEquals("Centro", endereco.bairro());
        assertEquals("13330000", endereco.cep());
        assertEquals("Indaiatuba", endereco.cidade());
    }

    @Test
    void shouldRemoveHyphenFromCep() {
        var endereco = new ClientePatchRequest.EnderecoRequest(
            null,
            null,
            null,
            "13330-000",
            null,
            null
        );

        assertEquals("13330000", endereco.cep());
    }

    @Test
    void shouldPreserveNullAddressFields() {
        var endereco = new ClientePatchRequest.EnderecoRequest(
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertNull(endereco.rua());
        assertNull(endereco.numero());
        assertNull(endereco.bairro());
        assertNull(endereco.cep());
        assertNull(endereco.cidade());
        assertNull(endereco.estado());
    }

    @Test
    void shouldAcceptNullCpf() {
        var request = new ClientePatchRequest(
            null,
            null,
            null
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectCpfWithLessThan11Characters() {
        var request = new ClientePatchRequest(
            "1234567890",
            null,
            null
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("CPF deve ter 11 caracteres")
        ));
    }

    @Test
    void shouldRejectCpfWithMoreThan11Characters() {
        var request = new ClientePatchRequest(
            "123456789012",
            null,
            null
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("CPF deve ter 11 caracteres")
        ));
    }

    @Test
    void shouldAcceptNullName() {
        var request = new ClientePatchRequest(
            null,
            null,
            null
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectNameExceeding150Characters() {
        var request = new ClientePatchRequest(
            null,
            "A".repeat(151),
            null
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Nome deve ter no máximo 150 caracteres")
        ));
    }

    @Test
    void shouldAcceptBlankName() {
        var request = new ClientePatchRequest(
            null,
            "   ",
            null
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectStreetExceeding150Characters() {
        var endereco = new ClientePatchRequest.EnderecoRequest(
            "A".repeat(151),
            null,
            null,
            null,
            null,
            null
        );

        var violations = validator.validate(
            new ClientePatchRequest(null, null, endereco)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Rua deve ter no máximo 150 caracteres")
        ));
    }

    @Test
    void shouldRejectNonPositiveNumero() {
        var endereco = new ClientePatchRequest.EnderecoRequest(
            null,
            0,
            null,
            null,
            null,
            null
        );

        var violations = validator.validate(
            new ClientePatchRequest(null, null, endereco)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Número deve ser positivo")
        ));
    }

    @Test
    void shouldAcceptNullNumero() {
        var endereco = new ClientePatchRequest.EnderecoRequest(
            null,
            null,
            null,
            null,
            null,
            null
        );

        var violations = validator.validate(
            new ClientePatchRequest(null, null, endereco)
        );

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectBairroExceeding100Characters() {
        var endereco = new ClientePatchRequest.EnderecoRequest(
            null,
            null,
            "A".repeat(101),
            null,
            null,
            null
        );

        var violations = validator.validate(
            new ClientePatchRequest(null, null, endereco)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Bairro deve ter no máximo 100 caracteres")
        ));
    }

    @Test
    void shouldRejectCepWithIncorrectLength() {
        var endereco = new ClientePatchRequest.EnderecoRequest(
            null,
            null,
            null,
            "1234567",
            null,
            null
        );

        var violations = validator.validate(
            new ClientePatchRequest(null, null, endereco)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("CEP deve conter exatamente 8 caracteres")
        ));
    }

    @Test
    void shouldRejectCidadeExceeding100Characters() {
        var endereco = new ClientePatchRequest.EnderecoRequest(
            null,
            null,
            null,
            null,
            "A".repeat(101),
            null
        );

        var violations = validator.validate(
            new ClientePatchRequest(null, null, endereco)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Cidade deve ter no máximo 100 caracteres")
        ));
    }

    @Test
    void shouldAcceptEmptyPatchRequest() {
        var request = new ClientePatchRequest(
            null,
            null,
            null
        );

        assertTrue(validator.validate(request).isEmpty());
    }
}