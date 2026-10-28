package com.example.simulacao.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerPatchRequestTest {
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
    void deveRemoverPontosETracosDoCpf() {
        var request = new CustomerPatchRequest(
            "123.456.789-03",
            null,
            null
        );

        assertEquals("12345678903", request.cpf());
    }

    @Test
    void deveManterCpfSemFormatacao() {
        var request = new CustomerPatchRequest(
            "12345678903",
            null,
            null
        );

        assertEquals("12345678903", request.cpf());
    }

    @Test
    void devePreservarCpfNulo() {
        var request = new CustomerPatchRequest(
            null,
            null,
            null
        );

        assertNull(request.cpf());
    }

    @Test
    void deveRemoverEspacosDoNome() {
        var request = new CustomerPatchRequest(
            null,
            "  Felipe Ferreira  ",
            null
        );

        assertEquals("Felipe Ferreira", request.name());
    }

    @Test
    void devePreservarNomeNulo() {
        var request = new CustomerPatchRequest(
            null,
            null,
            null
        );

        assertNull(request.name());
    }

    @Test
    void deveRemoverEspacosDosCamposDoEndereco() {
        var address = new CustomerPatchRequest.AddressRequest(
            "  Rua das Flores  ",
            null,
            "  Centro  ",
            " 13330000 ",
            " Indaiatuba ",
            null
        );

        assertEquals("Rua das Flores", address.rua());
        assertEquals("Centro", address.bairro());
        assertEquals("13330000", address.cep());
        assertEquals("Indaiatuba", address.cidade());
    }

    @Test
    void deveRemoverHifenDoCep() {
        var address = new CustomerPatchRequest.AddressRequest(
            null,
            null,
            null,
            "13330-000",
            null,
            null
        );

        assertEquals("13330000", address.cep());
    }

    @Test
    void devePreservarCamposDoEnderecoNulos() {
        var address = new CustomerPatchRequest.AddressRequest(
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertNull(address.rua());
        assertNull(address.numero());
        assertNull(address.bairro());
        assertNull(address.cep());
        assertNull(address.cidade());
        assertNull(address.state());
    }

    @Test
    void deveAceitarCpfNulo() {
        var request = new CustomerPatchRequest(
            null,
            null,
            null
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void deveRejeitarCpfComMenosDe11Caracteres() {
        var request = new CustomerPatchRequest(
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
    void deveRejeitarCpfComMaisDe11Caracteres() {
        var request = new CustomerPatchRequest(
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
    void deveAceitarNomeNulo() {
        var request = new CustomerPatchRequest(
            null,
            null,
            null
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void deveRejeitarNomeComMaisDe150Caracteres() {
        var request = new CustomerPatchRequest(
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
    void deveAceitarNomeEmBranco() {
        var request = new CustomerPatchRequest(
            null,
            "   ",
            null
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void deveRejeitarRuaComMaisDe150Caracteres() {
        var address = new CustomerPatchRequest.AddressRequest(
            "A".repeat(151),
            null,
            null,
            null,
            null,
            null
        );

        var violations = validator.validate(
            new CustomerPatchRequest(null, null, address)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Rua deve ter no máximo 150 caracteres")
        ));
    }

    @Test
    void deveRejeitarNumeroNaoPositivo() {
        var address = new CustomerPatchRequest.AddressRequest(
            null,
            0,
            null,
            null,
            null,
            null
        );

        var violations = validator.validate(
            new CustomerPatchRequest(null, null, address)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Número deve ser positivo")
        ));
    }

    @Test
    void deveAceitarNumeroNulo() {
        var address = new CustomerPatchRequest.AddressRequest(
            null,
            null,
            null,
            null,
            null,
            null
        );

        var violations = validator.validate(
            new CustomerPatchRequest(null, null, address)
        );

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveRejeitarBairroComMaisDe100Caracteres() {
        var address = new CustomerPatchRequest.AddressRequest(
            null,
            null,
            "A".repeat(101),
            null,
            null,
            null
        );

        var violations = validator.validate(
            new CustomerPatchRequest(null, null, address)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Bairro deve ter no máximo 100 caracteres")
        ));
    }

    @Test
    void deveRejeitarCepComTamanhoIncorreto() {
        var address = new CustomerPatchRequest.AddressRequest(
            null,
            null,
            null,
            "1234567",
            null,
            null
        );

        var violations = validator.validate(
            new CustomerPatchRequest(null, null, address)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("CEP deve conter exatamente 8 caracteres")
        ));
    }

    @Test
    void deveRejeitarCidadeComMaisDe100Caracteres() {
        var address = new CustomerPatchRequest.AddressRequest(
            null,
            null,
            null,
            null,
            "A".repeat(101),
            null
        );

        var violations = validator.validate(
            new CustomerPatchRequest(null, null, address)
        );

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Cidade deve ter no máximo 100 caracteres")
        ));
    }

    @Test
    void deveAceitarRequisicaoPatchVazia() {
        var request = new CustomerPatchRequest(
            null,
            null,
            null
        );

        assertTrue(validator.validate(request).isEmpty());
    }
}