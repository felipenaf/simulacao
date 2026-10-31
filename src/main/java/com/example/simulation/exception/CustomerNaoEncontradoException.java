package com.example.simulation.exception;

public class CustomerNaoEncontradoException extends RuntimeException {
    public CustomerNaoEncontradoException(String message) {
        super(message);
    }
}