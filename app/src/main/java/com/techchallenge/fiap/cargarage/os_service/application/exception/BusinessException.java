package com.techchallenge.fiap.cargarage.os_service.application.exception;

/**
 * Base exception for business rule violations.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
