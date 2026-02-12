package com.techchallenge.fiap.cargarage.os_service.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * DTO for updating Service Order status.
 */
@Builder
public record ServiceOrderStatusUpdateDto(
        @NotBlank(message = "status is required") String status) {
}
