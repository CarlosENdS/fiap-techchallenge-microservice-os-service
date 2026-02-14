package com.techchallenge.fiap.cargarage.os_service.application.dto;

/**
 * DTO for pagination request parameters.
 */
public record PageRequestDto(
        int page,
        int size) {
}
