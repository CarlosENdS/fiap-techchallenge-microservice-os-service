package com.techchallenge.fiap.cargarage.os_service.application.usecase;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging.ServiceOrderEventPublisher;

/**
 * Use case for cancelling a Service Order.
 * Part of the Saga compensation flow.
 */
@RequiredArgsConstructor
public class CancelServiceOrderUseCase {

    private final ServiceOrderGateway serviceOrderGateway;
    private final ServiceOrderEventPublisher eventPublisher;

    /**
     * Executes the use case to cancel a service order.
     *
     * @param id     the service order ID
     * @param reason the cancellation reason
     * @return the cancelled service order
     * @throws NotFoundException    if the service order is not found
     * @throws InvalidDataException if the order cannot be cancelled in its current
     *                              status
     */
    public ServiceOrder execute(Long id, String reason) {
        ServiceOrder existing = serviceOrderGateway.findById(id)
                .orElseThrow(() -> new NotFoundException("Service order not found with id: " + id));

        // Only allow cancellation from certain statuses
        ServiceOrderStatus current = existing.status();
        ServiceOrderStatus cancelled = ServiceOrderStatus.cancelled();

        if (!current.canTransitionTo(cancelled)) {
            throw new InvalidDataException(
                    "Cannot cancel order in status: " + current.value());
        }

        LocalDateTime now = LocalDateTime.now();
        ServiceOrder updated = existing.withStatusUpdated(cancelled, now);

        ServiceOrder savedOrder = serviceOrderGateway.update(updated);

        // Publish cancellation event for Saga compensation
        eventPublisher.publishOrderCancelled(savedOrder);

        return savedOrder;
    }
}
