package com.techchallenge.fiap.cargarage.os_service.application.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO for creating/updating a Service Order.
 */
@Builder
public record ServiceOrderRequestDto(
        @NotNull(message = "customerId is required") Long customerId,

        String customerName,

        @NotNull(message = "vehicleId is required") Long vehicleId,

        String vehicleLicensePlate,
        String vehicleModel,
        String vehicleBrand,
        String description,

        @Valid List<ServiceOrderItemRequestDto> services,

        @Valid List<ServiceOrderResourceRequestDto> resources) {
}
