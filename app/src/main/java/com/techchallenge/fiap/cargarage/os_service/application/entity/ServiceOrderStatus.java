package com.techchallenge.fiap.cargarage.os_service.application.entity;

import java.util.Objects;

import lombok.EqualsAndHashCode;
import com.techchallenge.fiap.cargarage.os_service.application.enums.ServiceOrderStatusEnum;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;

/**
 * Value object that represents service order status as a canonical String.
 * The enum ServiceOrderStatusEnum is used only to validate/normalize input
 * during creation.
 */
@EqualsAndHashCode
public final class ServiceOrderStatus {

    private final String status;

    private ServiceOrderStatus(String status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * Create a ServiceOrderStatus from a string. Normalizes the input and validates
     * against ServiceOrderStatusEnum.
     * Throws InvalidDataException when the provided string doesn't map to a known
     * status.
     */
    public static ServiceOrderStatus of(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status must not be null or blank");
        }
        String normalized = status.trim().toUpperCase();
        ServiceOrderStatusEnum enumVal = ServiceOrderStatusEnum.fromString(normalized);
        if (enumVal == null) {
            throw new InvalidDataException("Invalid order status: " + status);
        }
        return new ServiceOrderStatus(enumVal.name());
    }

    // Named factories for each status for convenience and readability in use-cases
    public static ServiceOrderStatus received() {
        return new ServiceOrderStatus(ServiceOrderStatusEnum.RECEIVED.name());
    }

    public static ServiceOrderStatus inDiagnosis() {
        return new ServiceOrderStatus(ServiceOrderStatusEnum.IN_DIAGNOSIS.name());
    }

    public static ServiceOrderStatus waitingApproval() {
        return new ServiceOrderStatus(ServiceOrderStatusEnum.WAITING_APPROVAL.name());
    }

    public static ServiceOrderStatus inExecution() {
        return new ServiceOrderStatus(ServiceOrderStatusEnum.IN_EXECUTION.name());
    }

    public static ServiceOrderStatus finished() {
        return new ServiceOrderStatus(ServiceOrderStatusEnum.FINISHED.name());
    }

    public static ServiceOrderStatus delivered() {
        return new ServiceOrderStatus(ServiceOrderStatusEnum.DELIVERED.name());
    }

    public static ServiceOrderStatus cancelled() {
        return new ServiceOrderStatus(ServiceOrderStatusEnum.CANCELLED.name());
    }

    // Predicate helpers
    public boolean isReceived() {
        return ServiceOrderStatusEnum.RECEIVED.name().equals(this.status);
    }

    public boolean isInDiagnosis() {
        return ServiceOrderStatusEnum.IN_DIAGNOSIS.name().equals(this.status);
    }

    public boolean isWaitingApproval() {
        return ServiceOrderStatusEnum.WAITING_APPROVAL.name().equals(this.status);
    }

    public boolean isInExecution() {
        return ServiceOrderStatusEnum.IN_EXECUTION.name().equals(this.status);
    }

    public boolean isFinished() {
        return ServiceOrderStatusEnum.FINISHED.name().equals(this.status);
    }

    public boolean isDelivered() {
        return ServiceOrderStatusEnum.DELIVERED.name().equals(this.status);
    }

    public boolean isCancelled() {
        return ServiceOrderStatusEnum.CANCELLED.name().equals(this.status);
    }

    public String value() {
        return status;
    }

    /**
     * Returns true if a transition from this status to target is allowed by
     * business rules.
     * Uses ServiceOrderStatusEnum for decision logic by mapping the stored strings
     * back to enum values.
     */
    public boolean canTransitionTo(ServiceOrderStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        ServiceOrderStatusEnum currentEnum = ServiceOrderStatusEnum.fromString(this.status);
        ServiceOrderStatusEnum targetEnum = ServiceOrderStatusEnum.fromString(targetStatus.status);
        if (currentEnum == null || targetEnum == null) {
            return false;
        }

        switch (currentEnum) {
            case RECEIVED:
                return targetEnum == ServiceOrderStatusEnum.IN_DIAGNOSIS
                        || targetEnum == ServiceOrderStatusEnum.CANCELLED;
            case IN_DIAGNOSIS:
                return targetEnum == ServiceOrderStatusEnum.WAITING_APPROVAL
                        || targetEnum == ServiceOrderStatusEnum.CANCELLED;
            case WAITING_APPROVAL:
                return targetEnum == ServiceOrderStatusEnum.IN_EXECUTION
                        || targetEnum == ServiceOrderStatusEnum.IN_DIAGNOSIS
                        || targetEnum == ServiceOrderStatusEnum.CANCELLED;
            case IN_EXECUTION:
                return targetEnum == ServiceOrderStatusEnum.FINISHED;
            case FINISHED:
                return targetEnum == ServiceOrderStatusEnum.DELIVERED;
            case DELIVERED:
                return false;
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return status;
    }
}
