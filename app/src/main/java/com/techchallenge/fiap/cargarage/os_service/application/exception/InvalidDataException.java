package com.techchallenge.fiap.cargarage.os_service.application.exception;

/**
 * Exception thrown when invalid data is provided.
 */
public class InvalidDataException extends BusinessException {

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
