package com.techchallenge.fiap.cargarage.os_service.application.gateway;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderPersistenceDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderItem;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderResource;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.interfaces.ServiceOrderDataSource;
import com.techchallenge.fiap.cargarage.os_service.application.presenter.ServiceOrderPresenter;

/**
 * Gateway for Service Order operations.
 * Acts as an adapter between the application layer and the data source.
 */
@RequiredArgsConstructor
public class ServiceOrderGateway {

    private final ServiceOrderDataSource serviceOrderDataSource;

    /**
     * Finds a service order by its ID.
     *
     * @param id the service order ID
     * @return an Optional containing the service order if found
     */
    public Optional<ServiceOrder> findById(Long id) {
        return serviceOrderDataSource.findById(id).map(this::toModel);
    }

    /**
     * Finds all service orders with pagination.
     *
     * @param pageRequest pagination parameters
     * @return a page of service orders
     */
    public PageDto<ServiceOrder> findAll(PageRequestDto pageRequest) {
        PageDto<ServiceOrderDto> dtoPage = serviceOrderDataSource.findAll(pageRequest);
        List<ServiceOrder> content = dtoPage.content().stream().map(this::toModel).toList();
        return new PageDto<>(content, dtoPage.totalElements(), dtoPage.pageNumber(), dtoPage.pageSize());
    }

    /**
     * Finds service orders by customer ID with pagination.
     *
     * @param customerId  the customer ID
     * @param pageRequest pagination parameters
     * @return a page of service orders
     */
    public PageDto<ServiceOrder> findByCustomerId(Long customerId, PageRequestDto pageRequest) {
        PageDto<ServiceOrderDto> dtoPage = serviceOrderDataSource.findByCustomerId(
                customerId, pageRequest);
        List<ServiceOrder> content = dtoPage.content().stream().map(this::toModel).toList();
        return new PageDto<>(content, dtoPage.totalElements(), dtoPage.pageNumber(), dtoPage.pageSize());
    }

    /**
     * Finds service orders by status with pagination.
     *
     * @param status      the status to filter by
     * @param pageRequest pagination parameters
     * @return a page of service orders
     */
    public PageDto<ServiceOrder> findByStatus(ServiceOrderStatus status, PageRequestDto pageRequest) {
        PageDto<ServiceOrderDto> dtoPage = serviceOrderDataSource.findByStatus(
                status.value(), pageRequest);
        List<ServiceOrder> content = dtoPage.content().stream().map(this::toModel).toList();
        return new PageDto<>(content, dtoPage.totalElements(), dtoPage.pageNumber(), dtoPage.pageSize());
    }

    /**
     * Inserts a new service order.
     *
     * @param orderModel the service order to insert
     * @return the inserted service order
     */
    public ServiceOrder insert(ServiceOrder orderModel) {
        ServiceOrderPersistenceDto persistence = toPersistenceDto(orderModel);
        ServiceOrderDto saved = serviceOrderDataSource.insert(persistence);
        return toModel(saved);
    }

    /**
     * Updates an existing service order.
     *
     * @param orderModel the service order to update
     * @return the updated service order
     */
    public ServiceOrder update(ServiceOrder orderModel) {
        ServiceOrderPersistenceDto persistence = toPersistenceDto(orderModel);
        ServiceOrderDto saved = serviceOrderDataSource.update(orderModel.id(), persistence);
        return toModel(saved);
    }

    /**
     * Deletes a service order by its ID.
     *
     * @param id the service order ID
     */
    public void deleteById(Long id) {
        serviceOrderDataSource.deleteById(id);
    }

    private ServiceOrderPersistenceDto toPersistenceDto(ServiceOrder model) {
        var requestDto = ServiceOrderPresenter.toRequestDtoFromModel(model);

        return ServiceOrderPersistenceDto.builder()
                .id(model.id())
                .customerId(requestDto.customerId())
                .customerName(requestDto.customerName())
                .vehicleId(requestDto.vehicleId())
                .vehicleLicensePlate(requestDto.vehicleLicensePlate())
                .vehicleModel(requestDto.vehicleModel())
                .vehicleBrand(requestDto.vehicleBrand())
                .description(requestDto.description())
                .status(model.status() != null ? model.status().value() : null)
                .totalPrice(model.totalPrice())
                .createdAt(model.createdAt())
                .updatedAt(model.updatedAt())
                .approvedAt(model.approvedAt())
                .finishedAt(model.finishedAt())
                .deliveredAt(model.deliveredAt())
                .services(requestDto.services())
                .resources(requestDto.resources())
                .build();
    }

    private ServiceOrder toModel(ServiceOrderDto dto) {
        List<ServiceOrderItem> services = dto.services() != null
                ? dto.services().stream()
                        .map(s -> ServiceOrderItem.buildServiceOrderItem(
                                s.id(),
                                s.serviceId(),
                                s.serviceName(),
                                s.serviceDescription(),
                                s.quantity(),
                                s.price(),
                                s.totalPrice()))
                        .toList()
                : List.of();

        List<ServiceOrderResource> resources = dto.resources() != null
                ? dto.resources().stream()
                        .map(r -> ServiceOrderResource.buildServiceOrderResource(
                                r.id(),
                                r.resourceId(),
                                r.resourceName(),
                                r.resourceDescription(),
                                r.resourceType(),
                                r.quantity(),
                                r.price(),
                                r.totalPrice()))
                        .toList()
                : List.of();

        return ServiceOrder.builder()
                .id(dto.id())
                .customerId(dto.customerId())
                .customerName(dto.customerName())
                .vehicleId(dto.vehicleId())
                .vehicleLicensePlate(dto.vehicleLicensePlate())
                .vehicleModel(dto.vehicleModel())
                .vehicleBrand(dto.vehicleBrand())
                .description(dto.description())
                .status(dto.status() != null ? ServiceOrderStatus.of(dto.status()) : null)
                .totalPrice(dto.totalPrice())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .approvedAt(dto.approvedAt())
                .finishedAt(dto.finishedAt())
                .deliveredAt(dto.deliveredAt())
                .services(services)
                .resources(resources)
                .build();
    }
}
