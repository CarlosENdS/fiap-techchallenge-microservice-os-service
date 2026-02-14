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

import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.ProcessApprovalUseCase;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging.ServiceOrderEventPublisher;

@ExtendWith(MockitoExtension.class)
class ProcessApprovalUseCaseTest {

    @Mock
    private ServiceOrderGateway gateway;

    @Mock
    private ServiceOrderEventPublisher eventPublisher;

    private ProcessApprovalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessApprovalUseCase(gateway, eventPublisher);
    }

    private ServiceOrder createOrderWaitingApproval(Long id) {
        return ServiceOrder.builder()
                .id(id)
                .customerId(1L)
                .customerName("John Doe")
                .vehicleId(2L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Civic")
                .vehicleBrand("Honda")
                .description("Sample complaint")
                .status(ServiceOrderStatus.waitingApproval())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .services(List.of())
                .resources(List.of())
                .build();
    }

    @Test
    @DisplayName("Should approve order and transition to IN_EXECUTION")
    void shouldApproveOrderAndTransitionToInExecution() {
        // Arrange
        Long orderId = 100L;
        ServiceOrder existingOrder = createOrderWaitingApproval(orderId);

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, true);

        // Assert
        assertNotNull(result);
        assertEquals(ServiceOrderStatus.inExecution().value(), result.status().value());
        assertNotNull(result.approvedAt());
        verify(eventPublisher).publishOrderApproved(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Should reject order and transition to IN_DIAGNOSIS")
    void shouldRejectOrderAndTransitionToInDiagnosis() {
        // Arrange
        Long orderId = 100L;
        ServiceOrder existingOrder = createOrderWaitingApproval(orderId);

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, false);

        // Assert
        assertNotNull(result);
        assertEquals(ServiceOrderStatus.inDiagnosis().value(), result.status().value());
        verify(eventPublisher).publishOrderRejected(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when order not found")
    void shouldThrowNotFoundExceptionWhenOrderNotFound() {
        // Arrange
        Long orderId = 999L;

        when(gateway.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> useCase.execute(orderId, true));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when order not in WAITING_APPROVAL status")
    void shouldThrowInvalidDataExceptionWhenOrderNotWaitingApproval() {
        // Arrange
        Long orderId = 100L;
        ServiceOrder existingOrder = ServiceOrder.builder()
                .id(orderId)
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

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> useCase.execute(orderId, true));
    }
}
