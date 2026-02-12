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
 * Use case for processing customer approval of a Service Order.
 */
@RequiredArgsConstructor
public class ProcessApprovalUseCase {

    private final ServiceOrderGateway serviceOrderGateway;
    private final ServiceOrderEventPublisher eventPublisher;

    /**
     * Executes the use case to process customer approval.
     *
     * @param id       the service order ID
     * @param approved whether the customer approved the order
     * @return the updated service order
     * @throws NotFoundException    if the service order is not found
     * @throws InvalidDataException if the order is not in WAITING_APPROVAL status
     */
    public ServiceOrder execute(Long id, boolean approved) {
        ServiceOrder existing = serviceOrderGateway.findById(id)
                .orElseThrow(() -> new NotFoundException("Service order not found with id: " + id));

        if (!existing.status().isWaitingApproval()) {
            throw new InvalidDataException(
                    "Service order is not waiting for approval. Current status: "
                            + existing.status().value());
        }

        LocalDateTime now = LocalDateTime.now();
        ServiceOrderStatus newStatus;

        if (approved) {
            newStatus = ServiceOrderStatus.inExecution();
        } else {
            // If not approved, go back to diagnosis for revision
            newStatus = ServiceOrderStatus.inDiagnosis();
        }

        ServiceOrder updated = existing.withStatusUpdated(newStatus, now);
        ServiceOrder savedOrder = serviceOrderGateway.update(updated);

        // Publish appropriate event
        if (approved) {
            eventPublisher.publishOrderApproved(savedOrder);
        } else {
            eventPublisher.publishOrderRejected(savedOrder);
        }

        return savedOrder;
    }
}
