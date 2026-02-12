package com.techchallenge.fiap.cargarage.os_service.infrastructure.database.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity for Service Order Resource (parts/supplies).
 */
@Data
@Entity
@Builder
@Table(name = "service_order_resource")
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private ServiceOrderEntity order;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "resource_description")
    private String resourceDescription;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
