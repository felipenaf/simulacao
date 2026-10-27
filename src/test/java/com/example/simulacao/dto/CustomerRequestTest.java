package com.example.simulacao.dto;

import com.example.simulacao.entity.Estado;
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
    private static ClienteRequest.EnderecoRequest enderecoCorreto;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();

        enderecoCorreto = new ClienteRequest.EnderecoRequest(
            "Rua das Flores",
            123,
            "Centro",
            "13330000",
            "Indaiatuba",
            Estado.SP
        );
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void deveAceitarRequestValida() {
        var request = new ClienteRequest(
            "12345678903",
            "Felipe Ferreira",
            enderecoCorreto
        );

        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveRejeitarCpfEmBranco() {
        var request = new ClienteRequest(
            "   ",
            "Felipe Ferreira",
            enderecoCorreto
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("CPF inválido")
        ));
    }

    @Test
    void deveRejeitarCpfComMaisDe11Digitos() {
        var request = new ClienteRequest(
            "123456789101",
            "Felipe Ferreira",
            enderecoCorreto
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
        var request = new ClienteRequest(
            "1234567891",
            "Felipe Ferreira",
            enderecoCorreto
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
        var request = new ClienteRequest(
            "123.456.789-03",
            "Felipe Ferreira",
            enderecoCorreto
        );

        assertEquals("12345678903", request.cpf());
    }

    @Test
    void deveManterCpfSemFormatacao() {
        var request = new ClienteRequest(
            "12345678903",
            "Felipe Ferreira",
            enderecoCorreto
        );

        assertEquals("12345678903", request.cpf());
    }

    @Test
    void devePreservarCpfNulo() {
        var request = new ClienteRequest(
            null,
            "Felipe Ferreira",
            enderecoCorreto
        );

        assertNull(request.cpf());
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        var request = new ClienteRequest(
            "12345678903",
            "",
            enderecoCorreto
        );

        var violations = validator.validate(request);

        assertTrue(
            violations.stream()
                .anyMatch(v -> v.getMessage().equals("Nome inválido"))
        );
    }

    @Test
    void deveRemoverEspacosDoNome() {
        var request = new ClienteRequest(
            "12345678903",
            "  Felipe Ferreira  ",
            enderecoCorreto
        );

        assertEquals("Felipe Ferreira", request.nome());
    }

    @Test
    void deveRejeitarNumeroNulo() {
        var endereco = new ClienteRequest.EnderecoRequest(
            "Rua das Flores",
            null,
            "Centro",
            "13330000",
            "Indaiatuba",
            Estado.SP
        );

        var request = new ClienteRequest(
            "12345678903",
            "Felipe Ferreira",
            endereco
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Número inválido")
        ));
    }

    @Test
    void deveRejeitarNumeroNaoPositivo() {
        var endereco = new ClienteRequest.EnderecoRequest(
            "Rua das Flores",
            0,
            "Centro",
            "13330000",
            "Indaiatuba",
            Estado.SP
        );

        var request = new ClienteRequest(
            "12345678903",
            "Felipe Ferreira",
            endereco
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Número deve ser positivo")
        ));
    }

    @Test
    void deveRejeitarEnderecoNulo() {
        var request = new ClienteRequest(
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
        var endereco = new ClienteRequest.EnderecoRequest(
            "  Rua das Flores  ",
            123,
            "  Centro  ",
            " 13330000 ",
            " Indaiatuba ",
            Estado.SP
        );

        assertEquals("Rua das Flores", endereco.rua());
        assertEquals("Centro", endereco.bairro());
        assertEquals("13330000", endereco.cep());
        assertEquals("Indaiatuba", endereco.cidade());
    }

    @Test
    void deveRejeitarRuaInvalida() {
        var endereco = new ClienteRequest.EnderecoRequest(
            "",
            123,
            "Centro",
            "13330000",
            "Indaiatuba",
            Estado.SP
        );

        var request = new ClienteRequest(
            "12345678903",
            "Felipe Ferreira",
            endereco
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Rua inválida")
        ));
    }

    @Test
    void deveRejeitarBairroInvalido() {
        var endereco = new ClienteRequest.EnderecoRequest(
            "Rua das Flores",
            123,
            "",
            "13330000",
            "Indaiatuba",
            Estado.SP
        );

        var request = new ClienteRequest(
            "12345678903",
            "Felipe Ferreira",
            endereco
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Bairro inválido")
        ));
    }

    @Test
    void deveRemoverHifenDoCep() {
        var endereco = new ClienteRequest.EnderecoRequest(
            "Rua das Flores",
            123,
            "Centro",
            "13330-000",
            "Indaiatuba",
            Estado.SP
        );

        var request = new ClienteRequest(
            "12345678903",
            "Felipe Ferreira",
            endereco
        );

        assertEquals("13330000", request.endereco().cep());
    }

    @Test
    void deveRejeitarCepInvalido() {
        var endereco = new ClienteRequest.EnderecoRequest(
            "Rua das Flores",
            123,
            "Centro",
            "133300001",
            "Indaiatuba",
            Estado.SP
        );

        var request = new ClienteRequest(
            "12345678903",
            "Felipe Ferreira",
            endereco
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("CEP deve conter exatamente 8 caracteres")
        ));
    }

    @Test
    void deveRejeitarEstadoInvalido() {
        var endereco = new ClienteRequest.EnderecoRequest(
            "Rua das Flores",
            123,
            "Centro",
            "13330-000",
            "Indaiatuba",
            null
        );

        var request = new ClienteRequest(
            "12345678903",
            "Felipe Ferreira",
            endereco
        );

        var violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().equals("Estado inválido")
        ));
    }
}