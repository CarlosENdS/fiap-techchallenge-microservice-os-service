package com.techchallenge.fiap.cargarage.os_service.application.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import lombok.Builder;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;

/**
 * Domain entity representing a Service Order (OS).
 * This entity encapsulates the business logic for service orders
 * in the automotive service domain.
 */
public record ServiceOrder(
        Long id,
        Long customerId,
        String customerName,
        Long vehicleId,
        String vehicleLicensePlate,
        String vehicleModel,
        String vehicleBrand,
        String description,
        ServiceOrderStatus status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime approvedAt,
        LocalDateTime finishedAt,
        LocalDateTime deliveredAt,
        List<ServiceOrderItem> services,
        List<ServiceOrderResource> resources) {

    public ServiceOrder {
        if (Objects.isNull(customerId)) {
            throw new InvalidDataException("Invalid ServiceOrder: customerId must not be null");
        }

        if (Objects.isNull(vehicleId)) {
            throw new InvalidDataException("Invalid ServiceOrder: vehicleId must not be null");
        }

        if (Objects.isNull(services)) {
            throw new InvalidDataException("Invalid ServiceOrder: services list must not be null");
        }
    }

    @Builder(builderMethodName = "builder")
    public static ServiceOrder buildServiceOrder(
            Long id,
            Long customerId,
            String customerName,
            Long vehicleId,
            String vehicleLicensePlate,
            String vehicleModel,
            String vehicleBrand,
            String description,
            ServiceOrderStatus status,
            BigDecimal totalPrice,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime approvedAt,
            LocalDateTime finishedAt,
            LocalDateTime deliveredAt,
            List<ServiceOrderItem> services,
            List<ServiceOrderResource> resources) {
        return new ServiceOrder(
                id, customerId, customerName, vehicleId, vehicleLicensePlate,
                vehicleModel, vehicleBrand, description, status, totalPrice,
                createdAt, updatedAt, approvedAt, finishedAt, deliveredAt,
                services, resources);
    }

    public ServiceOrder withId(Long id) {
        return new ServiceOrder(
                id, this.customerId, this.customerName, this.vehicleId,
                this.vehicleLicensePlate, this.vehicleModel, this.vehicleBrand,
                this.description, this.status, this.totalPrice, this.createdAt,
                this.updatedAt, this.approvedAt, this.finishedAt, this.deliveredAt,
                this.services, this.resources);
    }

    /**
     * Return a new ServiceOrder with the status changed to {@code newStatus} and
     * timestamps adjusted according to business rules:
     * - updatedAt is set to now
     * - when transitioning to IN_EXECUTION, approvedAt is set to now if not already
     * set
     * - when transitioning to FINISHED, finishedAt is set to now if not already set
     * - when transitioning to DELIVERED, deliveredAt is set to now if not already
     * set.
     */
    public ServiceOrder withStatusUpdated(ServiceOrderStatus newStatus, LocalDateTime now) {
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }

        LocalDateTime approved = this.approvedAt;
        LocalDateTime finished = this.finishedAt;
        LocalDateTime delivered = this.deliveredAt;

        if (newStatus.isInExecution() && approved == null) {
            approved = now;
        }

        if (newStatus.isFinished() && finished == null) {
            finished = now;
        }

        if (newStatus.isDelivered() && delivered == null) {
            delivered = now;
        }

        return new ServiceOrder(
                this.id, this.customerId, this.customerName, this.vehicleId,
                this.vehicleLicensePlate, this.vehicleModel, this.vehicleBrand,
                this.description, newStatus, this.totalPrice, this.createdAt,
                now, approved, finished, delivered, this.services, this.resources);
    }
}
