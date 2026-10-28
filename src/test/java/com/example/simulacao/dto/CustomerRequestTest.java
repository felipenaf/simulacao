package com.example.simulacao.dto;

import com.example.simulacao.entity.State;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRequestTest {
    private static Validator validator;
    private static ValidatorFactory validatorFactory;
    private static CustomerRequest.AddressRequest correctAddress;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();

        correctAddress = new CustomerRequest.AddressRequest(
            "Rua das Flores",
            123,
            "Centro",
            "13330000",
            "Indaiatuba",
            State.SP
        );
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void deveAceitarRequestValida() {
        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            correctAddress
        );

        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveRejeitarCpfEmBranco() {
        var request = new CustomerRequest(
            "   ",
            "Felipe Ferreira",
            correctAddress
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("CPF inválido")
        ));
    }

    @Test
    void deveRejeitarCpfComMaisDe11Digitos() {
        var request = new CustomerRequest(
            "123456789101",
            "Felipe Ferreira",
            correctAddress
        );

        var violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
            "CPF deve ter 11 caracteres",
            violations.iterator().next().getMessage()
        );
    }

    @Test
    void deveRejeitarCpfComMenosDe11Digitos() {
        var request = new CustomerRequest(
            "1234567891",
            "Felipe Ferreira",
            correctAddress
        );

        var violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
            "CPF deve ter 11 caracteres",
            violations.iterator().next().getMessage()
        );
    }

    @Test
    void deveRemoverPontosETracosDoCpf() {
        var request = new CustomerRequest(
            "123.456.789-03",
            "Felipe Ferreira",
            correctAddress
        );

        assertEquals("12345678903", request.cpf());
    }

    @Test
    void deveManterCpfSemFormatacao() {
        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            correctAddress
        );

        assertEquals("12345678903", request.cpf());
    }

    @Test
    void devePreservarCpfNulo() {
        var request = new CustomerRequest(
            null,
            "Felipe Ferreira",
            correctAddress
        );

        assertNull(request.cpf());
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        var request = new CustomerRequest(
            "12345678903",
            "",
            correctAddress
        );

        var violations = validator.validate(request);

        assertTrue(
            violations.stream()
                .anyMatch(v -> v.getMessage().equals("Nome inválido"))
        );
    }

    @Test
    void deveRemoverEspacosDoNome() {
        var request = new CustomerRequest(
            "12345678903",
            "  Felipe Ferreira  ",
            correctAddress
        );

        assertEquals("Felipe Ferreira", request.name());
    }

    @Test
    void deveRejeitarNumeroNulo() {
        var address = new CustomerRequest.AddressRequest(
            "Rua das Flores",
            null,
            "Centro",
            "13330000",
            "Indaiatuba",
            State.SP
        );

        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            address
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Número inválido")
        ));
    }

    @Test
    void deveRejeitarNumeroNaoPositivo() {
        CustomerRequest.AddressRequest address = new CustomerRequest.AddressRequest(
            "Rua das Flores",
            0,
            "Centro",
            "13330000",
            "Indaiatuba",
            State.SP
        );

        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            address
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Número deve ser positivo")
        ));
    }

    @Test
    void deveRejeitarEnderecoNulo() {
        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            null
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Endereço inválido")
        ));
    }

    @Test
    void deveRemoverEspacosDosCamposDoEndereco() {
        var address = new CustomerRequest.AddressRequest(
            "  Rua das Flores  ",
            123,
            "  Centro  ",
            " 13330000 ",
            " Indaiatuba ",
            State.SP
        );

        assertEquals("Rua das Flores", address.rua());
        assertEquals("Centro", address.bairro());
        assertEquals("13330000", address.cep());
        assertEquals("Indaiatuba", address.cidade());
    }

    @Test
    void deveRejeitarRuaInvalida() {
        var address = new CustomerRequest.AddressRequest(
            "",
            123,
            "Centro",
            "13330000",
            "Indaiatuba",
            State.SP
        );

        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            address
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Rua inválida")
        ));
    }

    @Test
    void deveRejeitarBairroInvalido() {
        var address = new CustomerRequest.AddressRequest(
            "Rua das Flores",
            123,
            "",
            "13330000",
            "Indaiatuba",
            State.SP
        );

        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            address
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Bairro inválido")
        ));
    }

    @Test
    void deveRemoverHifenDoCep() {
        var address = new CustomerRequest.AddressRequest(
            "Rua das Flores",
            123,
            "Centro",
            "13330-000",
            "Indaiatuba",
            State.SP
        );

        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            address
        );

        assertEquals("13330000", request.address().cep());
    }

    @Test
    void deveRejeitarCepInvalido() {
        var address = new CustomerRequest.AddressRequest(
            "Rua das Flores",
            123,
            "Centro",
            "133300001",
            "Indaiatuba",
            State.SP
        );

        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            address
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("CEP deve conter exatamente 8 caracteres")
        ));
    }

    @Test
    void deveRejeitarEstadoInvalido() {
        var address = new CustomerRequest.AddressRequest(
            "Rua das Flores",
            123,
            "Centro",
            "13330-000",
            "Indaiatuba",
            null
        );

        var request = new CustomerRequest(
            "12345678903",
            "Felipe Ferreira",
            address
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Estado inválido")
        ));
    }
}