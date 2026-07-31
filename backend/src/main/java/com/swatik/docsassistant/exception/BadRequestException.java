package com.swatik.docsassistant.exception;

// Throws for invalid client input.
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
