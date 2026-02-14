package com.techchallenge.fiap.cargarage.os_service.application.usecase;

import lombok.RequiredArgsConstructor;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;

/**
 * Use case for finding Service Orders.
 */
@RequiredArgsConstructor
public class FindServiceOrderUseCase {

    private final ServiceOrderGateway serviceOrderGateway;

    /**
     * Finds a service order by its ID.
     *
     * @param id the service order ID
     * @return the service order
     * @throws NotFoundException if the service order is not found
     */
    public ServiceOrder findById(Long id) {
        return serviceOrderGateway.findById(id)
                .orElseThrow(() -> new NotFoundException("Service order not found with id: " + id));
    }

    /**
     * Finds all service orders with pagination.
     *
     * @param pageRequest pagination parameters
     * @return a page of service orders
     */
    public PageDto<ServiceOrder> findAll(PageRequestDto pageRequest) {
        return serviceOrderGateway.findAll(pageRequest);
    }

    /**
     * Finds service orders by customer ID with pagination.
     *
     * @param customerId  the customer ID
     * @param pageRequest pagination parameters
     * @return a page of service orders for the customer
     */
    public PageDto<ServiceOrder> findByCustomerId(Long customerId, PageRequestDto pageRequest) {
        return serviceOrderGateway.findByCustomerId(customerId, pageRequest);
    }

    /**
     * Finds service orders by status with pagination.
     *
     * @param status      the status to filter by
     * @param pageRequest pagination parameters
     * @return a page of service orders with the specified status
     */
    public PageDto<ServiceOrder> findByStatus(String status, PageRequestDto pageRequest) {
        ServiceOrderStatus orderStatus = ServiceOrderStatus.of(status);
        return serviceOrderGateway.findByStatus(orderStatus, pageRequest);
    }
}
