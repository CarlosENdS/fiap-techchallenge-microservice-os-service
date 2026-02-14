package com.techchallenge.fiap.cargarage.os_service.application.entity;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.Builder;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;

/**
 * Domain entity representing a resource/part item in a Service Order.
 */
public record ServiceOrderResource(
        Long id,
        Long resourceId,
        String resourceName,
        String resourceDescription,
        String resourceType,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalPrice) {

    public ServiceOrderResource {
        if (Objects.isNull(resourceId)) {
            throw new InvalidDataException("Invalid ServiceOrderResource: resourceId must not be null");
        }

        if (Objects.isNull(quantity) || quantity <= 0) {
            throw new InvalidDataException(
                    "Invalid ServiceOrderResource: quantity must be greater than zero");
        }

        if (Objects.isNull(price)) {
            throw new InvalidDataException("Invalid ServiceOrderResource: price must not be null");
        }

        if (Objects.isNull(totalPrice)) {
            throw new InvalidDataException("Invalid ServiceOrderResource: totalPrice must not be null");
        }
    }

    @Builder
    public static ServiceOrderResource buildServiceOrderResource(
            Long id,
            Long resourceId,
            String resourceName,
            String resourceDescription,
            String resourceType,
            Integer quantity,
            BigDecimal price,
            BigDecimal totalPrice) {
        BigDecimal calculatedTotal = totalPrice != null
                ? totalPrice
                : price.multiply(BigDecimal.valueOf(quantity));
        return new ServiceOrderResource(
                id, resourceId, resourceName, resourceDescription, resourceType,
                quantity, price, calculatedTotal);
    }

    public ServiceOrderResource withId(Long id) {
        return new ServiceOrderResource(
                id, this.resourceId, this.resourceName, this.resourceDescription,
                this.resourceType, this.quantity, this.price, this.totalPrice);
    }
}
