package com.techchallenge.fiap.cargarage.os_service.application.presenter;

import java.util.List;

import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;

/**
 * Presenter class for converting between Service Order domain models and DTOs.
 */
public final class ServiceOrderPresenter {

    private ServiceOrderPresenter() {
    }

    /**
     * Converts a ServiceOrder domain model to a response DTO.
     */
    public static ServiceOrderDto toResponseDtoFromModel(ServiceOrder model) {
        if (model == null) {
            return null;
        }

        List<ServiceOrderItemDto> services = model.services() != null
                ? model.services().stream()
                        .map(service -> ServiceOrderItemDto.builder()
                                .id(service.id())
                                .serviceId(service.serviceId())
                                .serviceName(service.serviceName())
                                .serviceDescription(service.serviceDescription())
                                .price(service.price())
                                .quantity(service.quantity())
                                .totalPrice(service.totalPrice())
                                .build())
                        .toList()
                : List.of();

        List<ServiceOrderResourceDto> resources = model.resources() != null
                ? model.resources().stream()
                        .map(resource -> ServiceOrderResourceDto.builder()
                                .id(resource.id())
                                .resourceId(resource.resourceId())
                                .resourceName(resource.resourceName())
                                .resourceDescription(resource.resourceDescription())
                                .resourceType(resource.resourceType())
                                .price(resource.price())
                                .quantity(resource.quantity())
                                .totalPrice(resource.totalPrice())
                                .build())
                        .toList()
                : List.of();

        return ServiceOrderDto.builder()
                .id(model.id())
                .customerId(model.customerId())
                .customerName(model.customerName())
                .vehicleId(model.vehicleId())
                .vehicleLicensePlate(model.vehicleLicensePlate())
                .vehicleModel(model.vehicleModel())
                .vehicleBrand(model.vehicleBrand())
                .description(model.description())
                .status(model.status() != null ? model.status().value() : null)
                .totalPrice(model.totalPrice())
                .createdAt(model.createdAt())
                .updatedAt(model.updatedAt())
                .approvedAt(model.approvedAt())
                .finishedAt(model.finishedAt())
                .deliveredAt(model.deliveredAt())
                .services(services)
                .resources(resources)
                .build();
    }

    /**
     * Converts a ServiceOrder domain model to a request DTO.
     */
    public static ServiceOrderRequestDto toRequestDtoFromModel(ServiceOrder model) {
        if (model == null) {
            return null;
        }

        List<ServiceOrderItemRequestDto> services = model.services() != null
                ? model.services().stream()
                        .map(service -> ServiceOrderItemRequestDto.builder()
                                .serviceId(service.serviceId())
                                .serviceName(service.serviceName())
                                .serviceDescription(service.serviceDescription())
                                .price(service.price())
                                .quantity(service.quantity())
                                .build())
                        .toList()
                : List.of();

        List<ServiceOrderResourceRequestDto> resources = model.resources() != null
                ? model.resources().stream()
                        .map(resource -> ServiceOrderResourceRequestDto.builder()
                                .resourceId(resource.resourceId())
                                .resourceName(resource.resourceName())
                                .resourceDescription(resource.resourceDescription())
                                .resourceType(resource.resourceType())
                                .price(resource.price())
                                .quantity(resource.quantity())
                                .build())
                        .toList()
                : List.of();

        return ServiceOrderRequestDto.builder()
                .customerId(model.customerId())
                .customerName(model.customerName())
                .vehicleId(model.vehicleId())
                .vehicleLicensePlate(model.vehicleLicensePlate())
                .vehicleModel(model.vehicleModel())
                .vehicleBrand(model.vehicleBrand())
                .description(model.description())
                .services(services)
                .resources(resources)
                .build();
    }

    /**
     * Converts a ServiceOrder domain model to a status DTO.
     */
    public static ServiceOrderStatusDto toStatusDtoFromModel(ServiceOrder model) {
        if (model == null) {
            return null;
        }
        return ServiceOrderStatusDto.builder()
                .status(model.status() != null ? model.status().value() : null)
                .build();
    }
}
