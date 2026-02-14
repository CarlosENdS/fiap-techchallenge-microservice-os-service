package com.techchallenge.fiap.cargarage.os_service.application.usecase;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusUpdateDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging.ServiceOrderEventPublisher;

/**
 * Use case for updating Service Order status.
 */
@RequiredArgsConstructor
public class UpdateServiceOrderStatusUseCase {

    private final ServiceOrderGateway serviceOrderGateway;
    private final ServiceOrderEventPublisher eventPublisher;

    /**
     * Executes the use case to update the status of a service order.
     *
     * @param id        the service order ID
     * @param statusDto the new status
     * @return the updated service order
     * @throws NotFoundException    if the service order is not found
     * @throws InvalidDataException if the status transition is not valid
     */
    public ServiceOrder execute(Long id, ServiceOrderStatusUpdateDto statusDto) {
        ServiceOrder existing = serviceOrderGateway.findById(id)
                .orElseThrow(() -> new NotFoundException("Service order not found with id: " + id));

        ServiceOrderStatus current = existing.status();
        ServiceOrderStatus newStatus = ServiceOrderStatus.of(statusDto.status());

        if (!current.canTransitionTo(newStatus)) {
            throw new InvalidDataException(
                    "Invalid status transition from " + current + " to " + newStatus);
        }

        // Delegate timestamp handling to the model
        LocalDateTime now = LocalDateTime.now();
        ServiceOrder updated = existing.withStatusUpdated(newStatus, now);

        ServiceOrder savedOrder = serviceOrderGateway.update(updated);

        // Publish event based on the new status
        publishStatusChangeEvent(savedOrder, newStatus);

        return savedOrder;
    }

    private void publishStatusChangeEvent(ServiceOrder order, ServiceOrderStatus status) {
        if (status.isWaitingApproval()) {
            eventPublisher.publishOrderWaitingApproval(order);
        } else if (status.isInExecution()) {
            eventPublisher.publishOrderApproved(order);
        } else if (status.isFinished()) {
            eventPublisher.publishOrderFinished(order);
        } else if (status.isDelivered()) {
            eventPublisher.publishOrderDelivered(order);
        } else if (status.isCancelled()) {
            eventPublisher.publishOrderCancelled(order);
        }
    }
}
