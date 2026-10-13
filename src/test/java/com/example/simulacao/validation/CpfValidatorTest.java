package com.example.simulacao.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {
    @Test
    void shouldAcceptValidCpf() {
        assertTrue(CpfValidator.isValid("52998224725"));
    }

    @Test
    void shouldRejectCpfWithLessThan11Digits() {
        assertFalse(CpfValidator.isValid("1234567891"));
    }

    @Test
    void shouldRejectCpfWithMoreThan11Digits() {
        assertFalse(CpfValidator.isValid("123456789101"));
    }

    @Test
    void shouldAcceptFormattedCpf() {
        assertTrue(CpfValidator.isValid("529.982.247-25"));
    }
}