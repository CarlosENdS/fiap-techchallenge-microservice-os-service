package com.techchallenge.fiap.cargarage.os_service.application.dto;

import java.math.BigDecimal;

import lombok.Builder;

/**
 * DTO for Service Order Item (service) response.
 */
@Builder
public record ServiceOrderItemDto(
        Long id,
        Long serviceId,
        String serviceName,
        String serviceDescription,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalPrice) {
}
