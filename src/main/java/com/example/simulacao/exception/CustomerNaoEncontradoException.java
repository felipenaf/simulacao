package com.example.simulacao.exception;

public class CustomerNaoEncontradoException extends RuntimeException {
    public CustomerNaoEncontradoException(String message) {
        super(message);
    }
}