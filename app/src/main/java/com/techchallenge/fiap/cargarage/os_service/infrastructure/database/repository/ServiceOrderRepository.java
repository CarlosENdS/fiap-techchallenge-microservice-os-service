package com.techchallenge.fiap.cargarage.os_service.infrastructure.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techchallenge.fiap.cargarage.os_service.infrastructure.database.entity.ServiceOrderEntity;

/**
 * JPA repository for Service Order entities.
 */
@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrderEntity, Long> {

    /**
     * Finds service orders by customer ID.
     *
     * @param customerId the customer ID
     * @param pageable   pagination parameters
     * @return a page of service orders
     */
    Page<ServiceOrderEntity> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Finds service orders by status.
     *
     * @param status   the status to filter by
     * @param pageable pagination parameters
     * @return a page of service orders
     */
    Page<ServiceOrderEntity> findByStatus(String status, Pageable pageable);
}
