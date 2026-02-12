package com.techchallenge.fiap.cargarage.os_service.application.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * DTO for error message responses.
 */
@Builder
public record ErrorMessageDto(
        String error,
        String message,
        int status,
        String path,
        LocalDateTime timestamp) {
}
