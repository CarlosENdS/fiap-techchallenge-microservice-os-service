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
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;

/**
 * Use case for updating an existing Service Order.
 */
@RequiredArgsConstructor
public class UpdateServiceOrderUseCase {

    private final ServiceOrderGateway serviceOrderGateway;

    /**
     * Executes the use case to update a service order.
     *
     * @param id         the service order ID
     * @param requestDto the updated service order data
     * @return the updated service order
     * @throws NotFoundException    if the service order is not found
     * @throws InvalidDataException if the order cannot be updated in its current
     *                              status
     */
    public ServiceOrder execute(Long id, ServiceOrderRequestDto requestDto) {
        ServiceOrder existing = serviceOrderGateway.findById(id)
                .orElseThrow(() -> new NotFoundException("Service order not found with id: " + id));

        // Only allow updates when status is RECEIVED or IN_DIAGNOSIS
        if (!existing.status().isReceived() && !existing.status().isInDiagnosis()) {
            throw new InvalidDataException(
                    "Cannot update order in status: " + existing.status().value());
        }

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

        ServiceOrder updated = ServiceOrder.builder()
                .id(existing.id())
                .customerId(requestDto.customerId() != null
                        ? requestDto.customerId()
                        : existing.customerId())
                .customerName(requestDto.customerName() != null
                        ? requestDto.customerName()
                        : existing.customerName())
                .vehicleId(requestDto.vehicleId() != null
                        ? requestDto.vehicleId()
                        : existing.vehicleId())
                .vehicleLicensePlate(requestDto.vehicleLicensePlate() != null
                        ? requestDto.vehicleLicensePlate()
                        : existing.vehicleLicensePlate())
                .vehicleModel(requestDto.vehicleModel() != null
                        ? requestDto.vehicleModel()
                        : existing.vehicleModel())
                .vehicleBrand(requestDto.vehicleBrand() != null
                        ? requestDto.vehicleBrand()
                        : existing.vehicleBrand())
                .description(requestDto.description())
                .status(existing.status())
                .totalPrice(total)
                .createdAt(existing.createdAt())
                .updatedAt(LocalDateTime.now())
                .approvedAt(existing.approvedAt())
                .finishedAt(existing.finishedAt())
                .deliveredAt(existing.deliveredAt())
                .services(services)
                .resources(resources)
                .build();

        return serviceOrderGateway.update(updated);
    }
}
