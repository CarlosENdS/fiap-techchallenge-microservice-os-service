package com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging;

import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;

/**
 * Interface for publishing Service Order events to messaging system.
 * Part of the Saga pattern implementation.
 */
public interface ServiceOrderEventPublisher {

    /**
     * Publishes an event when a service order is created.
     *
     * @param order the created service order
     */
    void publishOrderCreated(ServiceOrder order);

    /**
     * Publishes an event when a service order is waiting for approval.
     *
     * @param order the service order waiting for approval
     */
    void publishOrderWaitingApproval(ServiceOrder order);

    /**
     * Publishes an event when a service order is approved.
     *
     * @param order the approved service order
     */
    void publishOrderApproved(ServiceOrder order);

    /**
     * Publishes an event when a service order is rejected.
     *
     * @param order the rejected service order
     */
    void publishOrderRejected(ServiceOrder order);

    /**
     * Publishes an event when a service order is finished.
     *
     * @param order the finished service order
     */
    void publishOrderFinished(ServiceOrder order);

    /**
     * Publishes an event when a service order is delivered.
     *
     * @param order the delivered service order
     */
    void publishOrderDelivered(ServiceOrder order);

    /**
     * Publishes an event when a service order is cancelled.
     * Part of Saga compensation flow.
     *
     * @param order the cancelled service order
     */
    void publishOrderCancelled(ServiceOrder order);
}
