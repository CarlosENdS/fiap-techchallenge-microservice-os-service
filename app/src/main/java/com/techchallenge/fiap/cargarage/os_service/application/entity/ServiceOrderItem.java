package com.techchallenge.fiap.cargarage.os_service.application.entity;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.Builder;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;

/**
 * Domain entity representing a service item in a Service Order.
 */
public record ServiceOrderItem(
        Long id,
        Long serviceId,
        String serviceName,
        String serviceDescription,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalPrice) {

    public ServiceOrderItem {
        if (Objects.isNull(serviceId)) {
            throw new InvalidDataException("Invalid ServiceOrderItem: serviceId must not be null");
        }

        if (Objects.isNull(quantity) || quantity <= 0) {
            throw new InvalidDataException(
                    "Invalid ServiceOrderItem: quantity must be greater than zero");
        }

        if (Objects.isNull(price)) {
            throw new InvalidDataException("Invalid ServiceOrderItem: price must not be null");
        }

        if (Objects.isNull(totalPrice)) {
            throw new InvalidDataException("Invalid ServiceOrderItem: totalPrice must not be null");
        }
    }

    @Builder
    public static ServiceOrderItem buildServiceOrderItem(
            Long id,
            Long serviceId,
            String serviceName,
            String serviceDescription,
            Integer quantity,
            BigDecimal price,
            BigDecimal totalPrice) {
        BigDecimal calculatedTotal = totalPrice != null
                ? totalPrice
                : price.multiply(BigDecimal.valueOf(quantity));
        return new ServiceOrderItem(
                id, serviceId, serviceName, serviceDescription, quantity, price, calculatedTotal);
    }

    public ServiceOrderItem withId(Long id) {
        return new ServiceOrderItem(
                id, this.serviceId, this.serviceName, this.serviceDescription,
                this.quantity, this.price, this.totalPrice);
    }
}
