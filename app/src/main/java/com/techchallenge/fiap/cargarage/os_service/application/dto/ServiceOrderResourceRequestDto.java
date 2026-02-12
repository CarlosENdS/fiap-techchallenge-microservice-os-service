package com.techchallenge.fiap.cargarage.os_service.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO for creating/updating a Service Order Resource (parts/supplies).
 */
@Builder
public record ServiceOrderResourceRequestDto(
        @NotNull(message = "resourceId is required") Long resourceId,

        String resourceName,
        String resourceDescription,
        String resourceType,

        BigDecimal price,

        @NotNull(message = "quantity is required") @Min(value = 1, message = "quantity must be at least 1") Integer quantity) {
}
