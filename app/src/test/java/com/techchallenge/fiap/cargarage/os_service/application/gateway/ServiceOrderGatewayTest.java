package com.techchallenge.fiap.cargarage.os_service.application.gateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderPersistenceDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.application.interfaces.ServiceOrderDataSource;

@ExtendWith(MockitoExtension.class)
class ServiceOrderGatewayTest {

    @Mock
    private ServiceOrderDataSource dataSource;

    private ServiceOrderGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new ServiceOrderGateway(dataSource);
    }

    private ServiceOrder createSampleOrder(Long id) {
        return ServiceOrder.builder()
                .id(id)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status(ServiceOrderStatus.received())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .services(List.of())
                .resources(List.of())
                .build();
    }

    private ServiceOrderDto createServiceOrderDto(Long id) {
        return ServiceOrderDto.builder()
                .id(id)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status("RECEIVED")
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .services(List.of())
                .resources(List.of())
                .build();
    }

    @Test
    @DisplayName("Should insert service order")
    void shouldInsertServiceOrder() {
        // Arrange
        ServiceOrder order = createSampleOrder(null);
        Long generatedId = 1L;
        ServiceOrderDto savedDto = createServiceOrderDto(generatedId);

        when(dataSource.insert(any(ServiceOrderPersistenceDto.class))).thenReturn(savedDto);

        // Act
        ServiceOrder result = gateway.insert(order);

        // Assert
        assertNotNull(result);
        assertEquals(generatedId, result.id());
        verify(dataSource).insert(any(ServiceOrderPersistenceDto.class));
    }

    @Test
    @DisplayName("Should update service order")
    void shouldUpdateServiceOrder() {
        // Arrange
        Long orderId = 1L;
        ServiceOrder order = createSampleOrder(orderId);
        ServiceOrderDto updatedDto = createServiceOrderDto(orderId);

        when(dataSource.update(eq(orderId), any(ServiceOrderPersistenceDto.class))).thenReturn(updatedDto);

        // Act
        ServiceOrder result = gateway.update(order);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.id());
        verify(dataSource).update(eq(orderId), any(ServiceOrderPersistenceDto.class));
    }

    @Test
    @DisplayName("Should find order by ID")
    void shouldFindOrderById() {
        // Arrange
        Long orderId = 1L;
        ServiceOrderDto dto = createServiceOrderDto(orderId);

        when(dataSource.findById(orderId)).thenReturn(Optional.of(dto));

        // Act
        Optional<ServiceOrder> result = gateway.findById(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().id());
    }

    @Test
    @DisplayName("Should return empty when order not found")
    void shouldReturnEmptyWhenOrderNotFound() {
        // Arrange
        Long orderId = 999L;
        when(dataSource.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Optional<ServiceOrder> result = gateway.findById(orderId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all orders with pagination")
    void shouldFindAllOrdersWithPagination() {
        // Arrange
        PageRequestDto pageRequest = new PageRequestDto(0, 10);
        List<ServiceOrderDto> dtos = List.of(
                createServiceOrderDto(1L),
                createServiceOrderDto(2L));
        PageDto<ServiceOrderDto> dtoPage = new PageDto<>(dtos, 2L, 0, 10);

        when(dataSource.findAll(pageRequest)).thenReturn(dtoPage);

        // Act
        PageDto<ServiceOrder> result = gateway.findAll(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());
    }

    @Test
    @DisplayName("Should find orders by customer ID")
    void shouldFindOrdersByCustomerId() {
        // Arrange
        Long customerId = 100L;
        PageRequestDto pageRequest = new PageRequestDto(0, 10);
        List<ServiceOrderDto> dtos = List.of(createServiceOrderDto(1L));
        PageDto<ServiceOrderDto> dtoPage = new PageDto<>(dtos, 1L, 0, 10);

        when(dataSource.findByCustomerId(customerId, pageRequest)).thenReturn(dtoPage);

        // Act
        PageDto<ServiceOrder> result = gateway.findByCustomerId(customerId, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Should find orders by status")
    void shouldFindOrdersByStatus() {
        // Arrange
        ServiceOrderStatus status = ServiceOrderStatus.inExecution();
        PageRequestDto pageRequest = new PageRequestDto(0, 10);
        List<ServiceOrderDto> dtos = List.of(createServiceOrderDto(1L));
        PageDto<ServiceOrderDto> dtoPage = new PageDto<>(dtos, 1L, 0, 10);

        when(dataSource.findByStatus("IN_EXECUTION", pageRequest)).thenReturn(dtoPage);

        // Act
        PageDto<ServiceOrder> result = gateway.findByStatus(status, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Should delete order by ID")
    void shouldDeleteOrderById() {
        // Arrange
        Long orderId = 1L;

        // Act
        gateway.deleteById(orderId);

        // Assert
        verify(dataSource).deleteById(orderId);
    }
}
