package com.techchallenge.fiap.cargarage.os_service.application.interfaces;

import java.util.Optional;

import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderPersistenceDto;

/**
 * Interface for Service Order data source operations.
 * This interface abstracts the data persistence layer.
 */
public interface ServiceOrderDataSource {

    /**
     * Inserts a new service order.
     *
     * @param requestDto the service order data to persist
     * @return the persisted service order
     */
    ServiceOrderDto insert(ServiceOrderPersistenceDto requestDto);

    /**
     * Updates an existing service order.
     *
     * @param id         the service order ID
     * @param requestDto the updated service order data
     * @return the updated service order
     */
    ServiceOrderDto update(Long id, ServiceOrderPersistenceDto requestDto);

    /**
     * Finds a service order by its ID.
     *
     * @param id the service order ID
     * @return an Optional containing the service order if found
     */
    Optional<ServiceOrderDto> findById(Long id);

    /**
     * Finds all service orders with pagination.
     *
     * @param pageRequest pagination parameters
     * @return a page of service orders
     */
    PageDto<ServiceOrderDto> findAll(PageRequestDto pageRequest);

    /**
     * Finds service orders by customer ID with pagination.
     *
     * @param customerId  the customer ID
     * @param pageRequest pagination parameters
     * @return a page of service orders
     */
    PageDto<ServiceOrderDto> findByCustomerId(Long customerId, PageRequestDto pageRequest);

    /**
     * Finds service orders by status with pagination.
     *
     * @param status      the status to filter by
     * @param pageRequest pagination parameters
     * @return a page of service orders
     */
    PageDto<ServiceOrderDto> findByStatus(String status, PageRequestDto pageRequest);

    /**
     * Deletes a service order by its ID.
     *
     * @param id the service order ID
     */
    void deleteById(Long id);
}
