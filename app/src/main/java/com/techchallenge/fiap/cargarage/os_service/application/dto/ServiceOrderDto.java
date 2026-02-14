package com.techchallenge.fiap.cargarage.os_service.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

/**
 * DTO for Service Order response.
 */
@Builder
public record ServiceOrderDto(
        Long id,
        Long customerId,
        String customerName,
        Long vehicleId,
        String vehicleLicensePlate,
        String vehicleModel,
        String vehicleBrand,
        String description,
        String status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime approvedAt,
        LocalDateTime finishedAt,
        LocalDateTime deliveredAt,
        List<ServiceOrderItemDto> services,
        List<ServiceOrderResourceDto> resources) {
}
