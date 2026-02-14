package com.techchallenge.fiap.cargarage.os_service.application.enums;

/**
 * Enum representing the possible statuses of a Service Order (OS).
 * Follows the workflow:
 * RECEIVED → IN_DIAGNOSIS → WAITING_APPROVAL → IN_EXECUTION → FINISHED →
 * DELIVERED
 * CANCELLED can be triggered from various states.
 */
public enum ServiceOrderStatusEnum {
    /** Initial status when the service order is received. */
    RECEIVED,
    /** Status when the vehicle is being diagnosed. */
    IN_DIAGNOSIS,
    /** Status when waiting for customer approval of the quote. */
    WAITING_APPROVAL,
    /** Status when the service is being executed. */
    IN_EXECUTION,
    /** Status when the service is finished. */
    FINISHED,
    /** Status when the vehicle has been delivered to the customer. */
    DELIVERED,
    /** Status when the order has been cancelled. */
    CANCELLED;

    /**
     * Parses a string to a ServiceOrderStatusEnum.
     *
     * @param status the string representation of the status
     * @return the corresponding enum value, or null if not found
     */
    public static ServiceOrderStatusEnum fromString(String status) {
        for (ServiceOrderStatusEnum orderStatus : ServiceOrderStatusEnum.values()) {
            if (orderStatus.name().equalsIgnoreCase(status)) {
                return orderStatus;
            }
        }
        return null;
    }
}
