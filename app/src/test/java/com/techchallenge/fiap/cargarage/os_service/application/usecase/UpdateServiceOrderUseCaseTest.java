package com.techchallenge.fiap.cargarage.os_service.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderItem;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderResource;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.exception.BusinessException;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;

@ExtendWith(MockitoExtension.class)
class UpdateServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderGateway gateway;

    private UpdateServiceOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateServiceOrderUseCase(gateway);
    }

    private ServiceOrder createExistingOrder(Long id, ServiceOrderStatus status) {
        return ServiceOrder.builder()
                .id(id)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Original complaint")
                .status(status)
                .totalPrice(new BigDecimal("300.00"))
                .services(List.of(ServiceOrderItem.builder()
                        .id(10L)
                        .serviceId(1L)
                        .serviceName("Old Service")
                        .serviceDescription("Old Service Description")
                        .quantity(1)
                        .price(new BigDecimal("100.00"))
                        .totalPrice(new BigDecimal("100.00"))
                        .build()))
                .resources(List.of(ServiceOrderResource.builder()
                        .id(20L)
                        .resourceId(2L)
                        .resourceName("Old Part")
                        .resourceDescription("Old Part Description")
                        .resourceType("PART")
                        .quantity(1)
                        .price(new BigDecimal("200.00"))
                        .totalPrice(new BigDecimal("200.00"))
                        .build()))
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .build();
    }

    @Test
    @DisplayName("Should update order in RECEIVED status")
    void shouldUpdateOrderInReceivedStatus() {
        // Arrange
        Long orderId = 1L;
        ServiceOrder existingOrder = createExistingOrder(orderId, ServiceOrderStatus.received());

        ServiceOrderItemRequestDto newItem = ServiceOrderItemRequestDto.builder()
                .serviceId(3L)
                .serviceName("New Service")
                .serviceDescription("New Service Description")
                .price(new BigDecimal("150.00"))
                .quantity(1)
                .build();
        ServiceOrderResourceRequestDto newResource = ServiceOrderResourceRequestDto.builder()
                .resourceId(4L)
                .resourceName("New Part")
                .resourceDescription("New Part Description")
                .resourceType("PART")
                .price(new BigDecimal("75.00"))
                .quantity(2)
                .build();

        ServiceOrderRequestDto updateDto = ServiceOrderRequestDto.builder()
                .customerId(existingOrder.customerId())
                .vehicleId(existingOrder.vehicleId())
                .description("Updated complaint")
                .services(List.of(newItem))
                .resources(List.of(newResource))
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated complaint", result.description());
        assertEquals(1, result.services().size());
        assertEquals("New Service", result.services().get(0).serviceName());
    }

    @Test
    @DisplayName("Should update order in IN_DIAGNOSIS status")
    void shouldUpdateOrderInDiagnosisStatus() {
        // Arrange
        Long orderId = 2L;
        ServiceOrder existingOrder = createExistingOrder(orderId, ServiceOrderStatus.inDiagnosis());

        ServiceOrderRequestDto updateDto = ServiceOrderRequestDto.builder()
                .customerId(existingOrder.customerId())
                .vehicleId(existingOrder.vehicleId())
                .description("Diagnosis updated")
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Diagnosis updated", result.description());
    }

    @Test
    @DisplayName("Should throw NotFoundException when order not found")
    void shouldThrowNotFoundExceptionWhenOrderNotFound() {
        // Arrange
        Long orderId = 3L;
        ServiceOrderRequestDto updateDto = ServiceOrderRequestDto.builder()
                .customerId(100L)
                .vehicleId(200L)
                .description("Updated complaint")
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> useCase.execute(orderId, updateDto));
    }

    @Test
    @DisplayName("Should throw BusinessException when updating order in WAITING_APPROVAL status")
    void shouldThrowBusinessExceptionWhenUpdatingWaitingApprovalOrder() {
        // Arrange
        Long orderId = 4L;
        ServiceOrder existingOrder = createExistingOrder(orderId, ServiceOrderStatus.waitingApproval());

        ServiceOrderRequestDto updateDto = ServiceOrderRequestDto.builder()
                .customerId(existingOrder.customerId())
                .vehicleId(existingOrder.vehicleId())
                .description("Updated complaint")
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(BusinessException.class, () -> useCase.execute(orderId, updateDto));
    }

    @Test
    @DisplayName("Should throw BusinessException when updating order in IN_EXECUTION status")
    void shouldThrowBusinessExceptionWhenUpdatingInExecutionOrder() {
        // Arrange
        Long orderId = 5L;
        ServiceOrder existingOrder = ServiceOrder.builder()
                .id(orderId)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Original complaint")
                .status(ServiceOrderStatus.inExecution())
                .totalPrice(new BigDecimal("300.00"))
                .services(List.of())
                .resources(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .build();

        ServiceOrderRequestDto updateDto = ServiceOrderRequestDto.builder()
                .customerId(existingOrder.customerId())
                .vehicleId(existingOrder.vehicleId())
                .description("Updated complaint")
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(BusinessException.class, () -> useCase.execute(orderId, updateDto));
    }

    @Test
    @DisplayName("Should throw BusinessException when updating finished order")
    void shouldThrowBusinessExceptionWhenUpdatingFinishedOrder() {
        // Arrange
        Long orderId = 6L;
        ServiceOrder existingOrder = createExistingOrder(orderId, ServiceOrderStatus.finished());

        ServiceOrderRequestDto updateDto = ServiceOrderRequestDto.builder()
                .customerId(existingOrder.customerId())
                .vehicleId(existingOrder.vehicleId())
                .description("Updated complaint")
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(BusinessException.class, () -> useCase.execute(orderId, updateDto));
    }
}
