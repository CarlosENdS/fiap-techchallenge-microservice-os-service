package com.techchallenge.fiap.cargarage.os_service.application.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
