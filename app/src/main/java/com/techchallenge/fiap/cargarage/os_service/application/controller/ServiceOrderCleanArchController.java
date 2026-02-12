package com.techchallenge.fiap.cargarage.os_service.application.controller;

import lombok.RequiredArgsConstructor;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderApprovalDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderExecutionTimeDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusUpdateDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.presenter.ServiceOrderPresenter;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CancelServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CreateServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.FindServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.GetServiceOrderExecutionTimeUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.ProcessApprovalUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderStatusUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderUseCase;

/**
 * Clean Architecture controller for Service Order operations.
 * Acts as the application layer entry point.
 */
@RequiredArgsConstructor
public class ServiceOrderCleanArchController {

    private final FindServiceOrderUseCase findServiceOrderUseCase;
    private final CreateServiceOrderUseCase createServiceOrderUseCase;
    private final UpdateServiceOrderUseCase updateServiceOrderUseCase;
    private final UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase;
    private final ProcessApprovalUseCase processApprovalUseCase;
    private final GetServiceOrderExecutionTimeUseCase getServiceOrderExecutionTimeUseCase;
    private final CancelServiceOrderUseCase cancelServiceOrderUseCase;

    /**
     * Finds a service order by its ID.
     */
    public ServiceOrderDto findById(Long id) {
        return ServiceOrderPresenter.toResponseDtoFromModel(
                findServiceOrderUseCase.findById(id));
    }

    /**
     * Finds all service orders with pagination.
     */
    public PageDto<ServiceOrderDto> findAll(int page, int size) {
        PageRequestDto pageRequest = new PageRequestDto(page, size);
        PageDto<ServiceOrder> modelPage = findServiceOrderUseCase.findAll(pageRequest);
        return new PageDto<>(
                modelPage.content().stream()
                        .map(ServiceOrderPresenter::toResponseDtoFromModel)
                        .toList(),
                modelPage.totalElements(),
                modelPage.pageNumber(),
                modelPage.pageSize());
    }

    /**
     * Finds service orders by customer ID with pagination.
     */
    public PageDto<ServiceOrderDto> findByCustomerId(Long customerId, int page, int size) {
        PageRequestDto pageRequest = new PageRequestDto(page, size);
        PageDto<ServiceOrder> modelPage = findServiceOrderUseCase.findByCustomerId(
                customerId, pageRequest);
        return new PageDto<>(
                modelPage.content().stream()
                        .map(ServiceOrderPresenter::toResponseDtoFromModel)
                        .toList(),
                modelPage.totalElements(),
                modelPage.pageNumber(),
                modelPage.pageSize());
    }

    /**
     * Finds service orders by status with pagination.
     */
    public PageDto<ServiceOrderDto> findByStatus(String status, int page, int size) {
        PageRequestDto pageRequest = new PageRequestDto(page, size);
        PageDto<ServiceOrder> modelPage = findServiceOrderUseCase.findByStatus(
                status, pageRequest);
        return new PageDto<>(
                modelPage.content().stream()
                        .map(ServiceOrderPresenter::toResponseDtoFromModel)
                        .toList(),
                modelPage.totalElements(),
                modelPage.pageNumber(),
                modelPage.pageSize());
    }

    /**
     * Gets execution time statistics.
     */
    public ServiceOrderExecutionTimeDto getExecutionTimeStatistics() {
        return getServiceOrderExecutionTimeUseCase.execute();
    }

    /**
     * Creates a new service order.
     */
    public ServiceOrderDto create(ServiceOrderRequestDto requestDto) {
        return ServiceOrderPresenter.toResponseDtoFromModel(
                createServiceOrderUseCase.execute(requestDto));
    }

    /**
     * Updates an existing service order.
     */
    public ServiceOrderDto update(Long id, ServiceOrderRequestDto requestDto) {
        return ServiceOrderPresenter.toResponseDtoFromModel(
                updateServiceOrderUseCase.execute(id, requestDto));
    }

    /**
     * Updates the status of a service order.
     */
    public ServiceOrderDto updateStatus(Long id, ServiceOrderStatusUpdateDto statusDto) {
        return ServiceOrderPresenter.toResponseDtoFromModel(
                updateServiceOrderStatusUseCase.execute(id, statusDto));
    }

    /**
     * Processes customer approval for a service order.
     */
    public ServiceOrderDto processApproval(Long id, ServiceOrderApprovalDto approvalDto) {
        return ServiceOrderPresenter.toResponseDtoFromModel(
                processApprovalUseCase.execute(id, approvalDto.approved()));
    }

    /**
     * Gets the status of a service order.
     */
    public ServiceOrderStatusDto getStatus(Long id) {
        ServiceOrder order = findServiceOrderUseCase.findById(id);
        return ServiceOrderPresenter.toStatusDtoFromModel(order);
    }

    /**
     * Cancels a service order.
     */
    public ServiceOrderDto cancel(Long id, String reason) {
        return ServiceOrderPresenter.toResponseDtoFromModel(
                cancelServiceOrderUseCase.execute(id, reason));
    }
}
