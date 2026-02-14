package com.techchallenge.fiap.cargarage.os_service.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO for creating/updating a Service Order Item (service).
 */
@Builder
public record ServiceOrderItemRequestDto(
        @NotNull(message = "serviceId is required") Long serviceId,

        String serviceName,
        String serviceDescription,

        BigDecimal price,

        @NotNull(message = "quantity is required") @Min(value = 1, message = "quantity must be at least 1") Integer quantity) {
}
