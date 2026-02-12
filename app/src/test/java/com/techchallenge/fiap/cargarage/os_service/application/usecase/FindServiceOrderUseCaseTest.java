package com.techchallenge.fiap.cargarage.os_service.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
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
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.FindServiceOrderUseCase;

@ExtendWith(MockitoExtension.class)
class FindServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderGateway gateway;

    private FindServiceOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindServiceOrderUseCase(gateway);
    }

    private ServiceOrder createSampleOrder(Long id) {
        return ServiceOrder.builder()
                .id(id)
                .customerId(1L)
                .customerName("John Doe")
                .vehicleId(2L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Civic")
                .vehicleBrand("Honda")
                .description("Sample complaint")
                .status(ServiceOrderStatus.received())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .services(List.of())
                .resources(List.of())
                .build();
    }

    @Test
    @DisplayName("Should find service order by ID")
    void shouldFindServiceOrderById() {
        // Arrange
        Long orderId = 100L;
        ServiceOrder expectedOrder = createSampleOrder(orderId);
        when(gateway.findById(orderId)).thenReturn(Optional.of(expectedOrder));

        // Act
        ServiceOrder result = useCase.findById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.id());
    }

    @Test
    @DisplayName("Should throw NotFoundException when order not found")
    void shouldThrowNotFoundExceptionWhenOrderNotFound() {
        // Arrange
        Long orderId = 999L;
        when(gateway.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> useCase.findById(orderId));
    }

    @Test
    @DisplayName("Should find all service orders with pagination")
    void shouldFindAllServiceOrdersWithPagination() {
        // Arrange
        PageRequestDto pageRequest = new PageRequestDto(0, 10);
        List<ServiceOrder> orders = List.of(
                createSampleOrder(1L),
                createSampleOrder(2L));
        PageDto<ServiceOrder> expectedPage = new PageDto<>(orders, 2, 0, 10);
        when(gateway.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        PageDto<ServiceOrder> result = useCase.findAll(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());
    }

    @Test
    @DisplayName("Should find orders by customer ID")
    void shouldFindOrdersByCustomerId() {
        // Arrange
        Long customerId = 1L;
        PageRequestDto pageRequest = new PageRequestDto(0, 10);
        List<ServiceOrder> orders = List.of(createSampleOrder(100L));
        PageDto<ServiceOrder> expectedPage = new PageDto<>(orders, 1, 0, 10);
        when(gateway.findByCustomerId(customerId, pageRequest)).thenReturn(expectedPage);

        // Act
        PageDto<ServiceOrder> result = useCase.findByCustomerId(customerId, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Should find orders by status")
    void shouldFindOrdersByStatus() {
        // Arrange
        String status = "IN_EXECUTION";
        PageRequestDto pageRequest = new PageRequestDto(0, 10);
        List<ServiceOrder> orders = List.of(createSampleOrder(100L));
        PageDto<ServiceOrder> expectedPage = new PageDto<>(orders, 1, 0, 10);
        when(gateway.findByStatus(ServiceOrderStatus.of(status), pageRequest)).thenReturn(expectedPage);

        // Act
        PageDto<ServiceOrder> result = useCase.findByStatus(status, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }
}
