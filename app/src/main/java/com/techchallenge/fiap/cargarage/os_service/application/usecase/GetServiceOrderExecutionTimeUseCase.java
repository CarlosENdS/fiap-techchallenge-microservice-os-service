package com.techchallenge.fiap.cargarage.os_service.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

import lombok.RequiredArgsConstructor;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderExecutionTimeDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;

/**
 * Use case for getting Service Order execution time statistics.
 */
@RequiredArgsConstructor
public class GetServiceOrderExecutionTimeUseCase {

    private final ServiceOrderGateway serviceOrderGateway;

    /**
     * Executes the use case to get execution time statistics.
     *
     * @return the execution time statistics
     */
    public ServiceOrderExecutionTimeDto execute() {
        // Get all orders for statistics
        PageRequestDto pageRequest = new PageRequestDto(0, Integer.MAX_VALUE);
        List<ServiceOrder> allOrders = serviceOrderGateway.findAll(pageRequest).content();

        long totalOrders = allOrders.size();
        long ordersInProgress = 0;
        long ordersFinished = 0;
        long ordersDelivered = 0;

        BigDecimal totalExecutionHours = BigDecimal.ZERO;
        BigDecimal minExecutionHours = null;
        BigDecimal maxExecutionHours = null;
        int finishedOrderCount = 0;

        for (ServiceOrder order : allOrders) {
            if (order.status().isInExecution()) {
                ordersInProgress++;
            } else if (order.status().isFinished()) {
                ordersFinished++;
            } else if (order.status().isDelivered()) {
                ordersDelivered++;
            }

            // Calculate execution time for finished/delivered orders
            if ((order.status().isFinished() || order.status().isDelivered())
                    && order.approvedAt() != null
                    && order.finishedAt() != null) {

                Duration duration = Duration.between(order.approvedAt(), order.finishedAt());
                BigDecimal hours = BigDecimal.valueOf(duration.toMinutes())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                totalExecutionHours = totalExecutionHours.add(hours);
                finishedOrderCount++;

                if (minExecutionHours == null || hours.compareTo(minExecutionHours) < 0) {
                    minExecutionHours = hours;
                }
                if (maxExecutionHours == null || hours.compareTo(maxExecutionHours) > 0) {
                    maxExecutionHours = hours;
                }
            }
        }

        BigDecimal avgExecutionHours = finishedOrderCount > 0
                ? totalExecutionHours.divide(BigDecimal.valueOf(finishedOrderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ServiceOrderExecutionTimeDto.builder()
                .totalOrders(totalOrders)
                .avgExecutionTimeHours(avgExecutionHours)
                .minExecutionTimeHours(minExecutionHours != null ? minExecutionHours : BigDecimal.ZERO)
                .maxExecutionTimeHours(maxExecutionHours != null ? maxExecutionHours : BigDecimal.ZERO)
                .ordersInProgress(ordersInProgress)
                .ordersFinished(ordersFinished)
                .ordersDelivered(ordersDelivered)
                .build();
    }
}
