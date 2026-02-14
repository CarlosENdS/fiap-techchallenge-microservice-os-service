package com.techchallenge.fiap.cargarage.os_service.application.presenter;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderItem;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderResource;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.presenter.ServiceOrderPresenter;

class ServiceOrderPresenterTest {

    @Test
    @DisplayName("Should convert ServiceOrder to ServiceOrderDto")
    void shouldConvertServiceOrderToDto() {
        // Arrange
        Long orderId = 1L;
        Long customerId = 100L;
        Long vehicleId = 200L;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime approvedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime finishedAt = LocalDateTime.now();

        ServiceOrderItem item = ServiceOrderItem.builder()
                .id(1L)
                .serviceId(10L)
                .serviceName("Oil Change")
                .serviceDescription("Engine oil change")
                .quantity(1)
                .price(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("150.00"))
                .build();

        ServiceOrderResource resource = ServiceOrderResource.builder()
                .id(1L)
                .resourceId(20L)
                .resourceName("Motor Oil")
                .resourceDescription("Synthetic motor oil")
                .resourceType("PART")
                .quantity(5)
                .price(new BigDecimal("25.00"))
                .totalPrice(new BigDecimal("125.00"))
                .build();

        ServiceOrder order = ServiceOrder.builder()
                .id(orderId)
                .customerId(customerId)
                .customerName("Test Customer")
                .vehicleId(vehicleId)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Sedan")
                .vehicleBrand("TestBrand")
                .description("Engine noise complaint")
                .status(ServiceOrderStatus.finished())
                .totalPrice(new BigDecimal("275.00"))
                .createdAt(createdAt)
                .updatedAt(null)
                .approvedAt(approvedAt)
                .finishedAt(finishedAt)
                .deliveredAt(null)
                .services(List.of(item))
                .resources(List.of(resource))
                .build();

        // Act
        ServiceOrderDto result = ServiceOrderPresenter.toResponseDtoFromModel(order);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.id());
        assertEquals(customerId, result.customerId());
        assertEquals(vehicleId, result.vehicleId());
        assertEquals("Engine noise complaint", result.description());
        assertEquals("FINISHED", result.status());
        assertEquals(new BigDecimal("275.00"), result.totalPrice());
        assertEquals(1, result.services().size());
        assertEquals(1, result.resources().size());
        assertEquals("Oil Change", result.services().get(0).serviceName());
        assertEquals("Motor Oil", result.resources().get(0).resourceName());
        assertEquals(createdAt, result.createdAt());
        assertEquals(approvedAt, result.approvedAt());
        assertEquals(finishedAt, result.finishedAt());
    }

    @Test
    @DisplayName("Should handle null values in ServiceOrder")
    void shouldHandleNullValuesInServiceOrder() {
        // Arrange
        Long orderId = 2L;
        ServiceOrder order = ServiceOrder.builder()
                .id(orderId)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("XYZ-9999")
                .vehicleModel("Hatch")
                .vehicleBrand("TestBrand")
                .description(null)
                .status(ServiceOrderStatus.received())
                .totalPrice(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .services(List.of())
                .resources(List.of())
                .build();

        // Act
        ServiceOrderDto result = ServiceOrderPresenter.toResponseDtoFromModel(order);

        // Assert
        assertNotNull(result);
        assertNull(result.description());
        assertNull(result.approvedAt());
        assertNull(result.finishedAt());
        assertNull(result.deliveredAt());
        assertTrue(result.services().isEmpty());
        assertTrue(result.resources().isEmpty());
    }

    @Test
    @DisplayName("Should convert cancelled order correctly")
    void shouldConvertCancelledOrderCorrectly() {
        // Arrange
        ServiceOrder cancelledOrder = ServiceOrder.builder()
                .id(3L)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("DEF-5678")
                .vehicleModel("SUV")
                .vehicleBrand("TestBrand")
                .description("Complaint")
                .status(ServiceOrderStatus.cancelled())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .services(List.of())
                .resources(List.of())
                .build();

        // Act
        ServiceOrderDto result = ServiceOrderPresenter.toResponseDtoFromModel(cancelledOrder);

        // Assert
        assertNotNull(result);
        assertEquals("CANCELLED", result.status());
    }
}
