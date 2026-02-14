package com.techchallenge.fiap.cargarage.os_service.application.dto;

import java.math.BigDecimal;

import lombok.Builder;

/**
 * DTO for execution time statistics.
 */
@Builder
public record ServiceOrderExecutionTimeDto(
        long totalOrders,
        BigDecimal avgExecutionTimeHours,
        BigDecimal minExecutionTimeHours,
        BigDecimal maxExecutionTimeHours,
        long ordersInProgress,
        long ordersFinished,
        long ordersDelivered) {
}
