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

        return savedOrder;
    }
}
