package com.techchallenge.fiap.cargarage.os_service.application.dto;

import lombok.Builder;

/**
 * DTO for Service Order status response.
 */
@Builder
public record ServiceOrderStatusDto(
        String status) {
}
