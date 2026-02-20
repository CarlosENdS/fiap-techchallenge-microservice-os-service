package com.techchallenge.fiap.cargarage.os_service.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderItem;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderResource;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging.ServiceOrderEventPublisher;

/**
 * Use case for creating a new Service Order.
 */
@RequiredArgsConstructor
public class CreateServiceOrderUseCase {

    private final ServiceOrderGateway serviceOrderGateway;
    private final ServiceOrderEventPublisher eventPublisher;

    /**
     * Executes the use case to create a new service order.
     *
     * @param requestDto the service order request data
     * @return the created service order
     */
    public ServiceOrder execute(ServiceOrderRequestDto requestDto) {
        List<ServiceOrderItem> services = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (requestDto.services() != null) {
            for (ServiceOrderItemRequestDto serviceRequest : requestDto.services()) {
                BigDecimal servicePrice = serviceRequest.price() != null
                        ? serviceRequest.price()
                        : BigDecimal.ZERO;
                BigDecimal serviceTotal = servicePrice
                        .multiply(BigDecimal.valueOf(serviceRequest.quantity()));

                ServiceOrderItem item = ServiceOrderItem.buildServiceOrderItem(
                        null,
                        serviceRequest.serviceId(),
                        serviceRequest.serviceName(),
                        serviceRequest.serviceDescription(),
                        serviceRequest.quantity(),
                        servicePrice,
                        serviceTotal);
                services.add(item);
                total = total.add(serviceTotal);
            }
        }

        List<ServiceOrderResource> resources = new ArrayList<>();
        if (requestDto.resources() != null) {
            for (ServiceOrderResourceRequestDto resourceRequest : requestDto.resources()) {
                BigDecimal resourcePrice = resourceRequest.price() != null
                        ? resourceRequest.price()
                        : BigDecimal.ZERO;
                BigDecimal resourceTotal = resourcePrice
                        .multiply(BigDecimal.valueOf(resourceRequest.quantity()));

                ServiceOrderResource resource = ServiceOrderResource.buildServiceOrderResource(
                        null,
                        resourceRequest.resourceId(),
                        resourceRequest.resourceName(),
                        resourceRequest.resourceDescription(),
                        resourceRequest.resourceType(),
                        resourceRequest.quantity(),
                        resourcePrice,
                        resourceTotal);
                resources.add(resource);
                total = total.add(resourceTotal);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        ServiceOrder order = ServiceOrder.builder()
                .id(null)
                .customerId(requestDto.customerId())
                .customerName(requestDto.customerName())
                .vehicleId(requestDto.vehicleId())
                .vehicleLicensePlate(requestDto.vehicleLicensePlate())
                .vehicleModel(requestDto.vehicleModel())
                .vehicleBrand(requestDto.vehicleBrand())
                .description(requestDto.description())
                .status(ServiceOrderStatus.received())
                .totalPrice(total)
                .createdAt(now)
                .updatedAt(now)
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .services(services)
                .resources(resources)
                .build();

        ServiceOrder savedOrder = serviceOrderGateway.insert(order);

        // Publish event for Saga/integration
        eventPublisher.publishOrderCreated(savedOrder);

        // Auto-advance: when the order is created with a complete quote
        // (services/resources with prices), it is already diagnosed.
        // Automatically transition RECEIVED → IN_DIAGNOSIS → WAITING_APPROVAL
        // so the Saga can proceed without manual intervention.
        if (hasCompleteQuote(services, resources, total)) {
            LocalDateTime advanceTime = LocalDateTime.now();

            ServiceOrder inDiagnosis = savedOrder.withStatusUpdated(
                    ServiceOrderStatus.inDiagnosis(), advanceTime);
            serviceOrderGateway.update(inDiagnosis);

            ServiceOrder waitingApproval = inDiagnosis.withStatusUpdated(
                    ServiceOrderStatus.waitingApproval(), advanceTime);
            savedOrder = serviceOrderGateway.update(waitingApproval);

            eventPublisher.publishOrderWaitingApproval(savedOrder);
        }

        return savedOrder;
    }

    /**
     * A quote is considered complete when there is at least one service or resource
     * and the total price is greater than zero (i.e., items have prices assigned).
     */
    private boolean hasCompleteQuote(List<ServiceOrderItem> services,
            List<ServiceOrderResource> resources,
            BigDecimal total) {
        boolean hasItems = (services != null && !services.isEmpty())
                || (resources != null && !resources.isEmpty());
        return hasItems && total.compareTo(BigDecimal.ZERO) > 0;
    }
}
