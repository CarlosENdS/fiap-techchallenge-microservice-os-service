package com.techchallenge.fiap.cargarage.os_service.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderExecutionTimeDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.GetServiceOrderExecutionTimeUseCase;

@ExtendWith(MockitoExtension.class)
class GetServiceOrderExecutionTimeUseCaseTest {

    @Mock
    private ServiceOrderGateway gateway;

    private GetServiceOrderExecutionTimeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetServiceOrderExecutionTimeUseCase(gateway);
    }

    @Test
    @DisplayName("Should calculate execution time statistics for finished orders")
    void shouldCalculateExecutionTimeForFinishedOrders() {
        // Arrange
        LocalDateTime approvedAt = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2024, 1, 15, 12, 30);

        ServiceOrder finishedOrder = ServiceOrder.builder()
                .id(1L)
                .customerId(1L)
                .customerName("John Doe")
                .vehicleId(2L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Civic")
                .vehicleBrand("Honda")
                .description("Sample complaint")
                .status(ServiceOrderStatus.finished())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(approvedAt)
                .finishedAt(finishedAt)
                .services(List.of())
                .resources(List.of())
                .build();

        PageDto<ServiceOrder> pageDto = new PageDto<>(List.of(finishedOrder), 1, 0, Integer.MAX_VALUE);
        when(gateway.findAll(any())).thenReturn(pageDto);

        // Act
        ServiceOrderExecutionTimeDto result = useCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.totalOrders());
        assertEquals(1, result.ordersFinished());
        assertEquals(0, result.ordersInProgress());
        assertEquals(0, result.ordersDelivered());
        // 12:30 - 9:00 = 3 hours 30 minutes = 3.50 hours
        assertEquals(new BigDecimal("3.50"), result.avgExecutionTimeHours());
    }

    @Test
    @DisplayName("Should calculate execution time statistics for delivered orders")
    void shouldCalculateExecutionTimeForDeliveredOrders() {
        // Arrange
        LocalDateTime approvedAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2024, 1, 15, 15, 0);

        ServiceOrder deliveredOrder = ServiceOrder.builder()
                .id(1L)
                .customerId(1L)
                .customerName("John Doe")
                .vehicleId(2L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Civic")
                .vehicleBrand("Honda")
                .description("Sample complaint")
                .status(ServiceOrderStatus.delivered())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(approvedAt)
                .finishedAt(finishedAt)
                .deliveredAt(LocalDateTime.now())
                .services(List.of())
                .resources(List.of())
                .build();

        PageDto<ServiceOrder> pageDto = new PageDto<>(List.of(deliveredOrder), 1, 0, Integer.MAX_VALUE);
        when(gateway.findAll(any())).thenReturn(pageDto);

        // Act
        ServiceOrderExecutionTimeDto result = useCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.ordersDelivered());
        // 15:00 - 10:00 = 5 hours
        assertEquals(new BigDecimal("5.00"), result.avgExecutionTimeHours());
    }

    @Test
    @DisplayName("Should return zero statistics when no orders exist")
    void shouldReturnZeroStatisticsWhenNoOrdersExist() {
        // Arrange
        PageDto<ServiceOrder> emptyPage = new PageDto<>(List.of(), 0, 0, Integer.MAX_VALUE);
        when(gateway.findAll(any())).thenReturn(emptyPage);

        // Act
        ServiceOrderExecutionTimeDto result = useCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.totalOrders());
        assertEquals(BigDecimal.ZERO, result.avgExecutionTimeHours());
        assertEquals(BigDecimal.ZERO, result.minExecutionTimeHours());
        assertEquals(BigDecimal.ZERO, result.maxExecutionTimeHours());
    }

    @Test
    @DisplayName("Should count orders in progress correctly")
    void shouldCountOrdersInProgressCorrectly() {
        // Arrange
        ServiceOrder inProgressOrder = ServiceOrder.builder()
                .id(1L)
                .customerId(1L)
                .customerName("John Doe")
                .vehicleId(2L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Civic")
                .vehicleBrand("Honda")
                .description("Sample complaint")
                .status(ServiceOrderStatus.inExecution())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.now())
                .services(List.of())
                .resources(List.of())
                .build();

        PageDto<ServiceOrder> pageDto = new PageDto<>(List.of(inProgressOrder), 1, 0, Integer.MAX_VALUE);
        when(gateway.findAll(any())).thenReturn(pageDto);

        // Act
        ServiceOrderExecutionTimeDto result = useCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.ordersInProgress());
        assertEquals(0, result.ordersFinished());
        assertEquals(BigDecimal.ZERO, result.avgExecutionTimeHours());
    }

    @Test
    @DisplayName("Should calculate min and max execution times correctly")
    void shouldCalculateMinMaxExecutionTimesCorrectly() {
        // Arrange
        // Order 1: 2 hours execution
        ServiceOrder order1 = ServiceOrder.builder()
                .id(1L)
                .customerId(1L)
                .customerName("John Doe")
                .vehicleId(2L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Civic")
                .vehicleBrand("Honda")
                .description("Order 1")
                .status(ServiceOrderStatus.finished())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .finishedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .services(List.of())
                .resources(List.of())
                .build();

        // Order 2: 4 hours execution
        ServiceOrder order2 = ServiceOrder.builder()
                .id(2L)
                .customerId(1L)
                .customerName("Jane Doe")
                .vehicleId(3L)
                .vehicleLicensePlate("XYZ-5678")
                .vehicleModel("Accord")
                .vehicleBrand("Honda")
                .description("Order 2")
                .status(ServiceOrderStatus.finished())
                .totalPrice(new BigDecimal("800.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.of(2024, 1, 15, 9, 0))
                .finishedAt(LocalDateTime.of(2024, 1, 15, 13, 0))
                .services(List.of())
                .resources(List.of())
                .build();

        PageDto<ServiceOrder> pageDto = new PageDto<>(List.of(order1, order2), 2, 0, Integer.MAX_VALUE);
        when(gateway.findAll(any())).thenReturn(pageDto);

        // Act
        ServiceOrderExecutionTimeDto result = useCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.totalOrders());
        assertEquals(new BigDecimal("2.00"), result.minExecutionTimeHours());
        assertEquals(new BigDecimal("4.00"), result.maxExecutionTimeHours());
        // Average: (2 + 4) / 2 = 3 hours
        assertEquals(new BigDecimal("3.00"), result.avgExecutionTimeHours());
    }
}
