package com.example.simulation.exception;

public class CpfAlreadyAssociatedException extends RuntimeException {
    public CpfAlreadyAssociatedException(String message) {
        super(message);
    }
}