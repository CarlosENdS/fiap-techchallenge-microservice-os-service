package com.techchallenge.fiap.cargarage.os_service.application.dto;

import java.math.BigDecimal;

import lombok.Builder;

/**
 * DTO for Service Order Resource (parts/supplies) response.
 */
@Builder
public record ServiceOrderResourceDto(
        Long id,
        Long resourceId,
        String resourceName,
        String resourceDescription,
        String resourceType,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalPrice) {
}
