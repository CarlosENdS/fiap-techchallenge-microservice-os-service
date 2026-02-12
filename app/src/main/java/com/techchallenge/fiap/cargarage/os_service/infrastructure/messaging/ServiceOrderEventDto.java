package com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * DTO for Service Order events sent to messaging system.
 */
@Builder
public record ServiceOrderEventDto(
        String eventType,
        Long orderId,
        Long customerId,
        String customerName,
        Long vehicleId,
        String vehicleLicensePlate,
        String status,
        String description,
        LocalDateTime timestamp) {
}
