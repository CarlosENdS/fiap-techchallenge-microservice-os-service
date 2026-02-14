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
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CancelServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging.ServiceOrderEventPublisher;

@ExtendWith(MockitoExtension.class)
class CancelServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderGateway gateway;

    @Mock
    private ServiceOrderEventPublisher eventPublisher;

    private CancelServiceOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelServiceOrderUseCase(gateway, eventPublisher);
    }

    private ServiceOrder createOrderWithStatus(Long id, ServiceOrderStatus status) {
        return ServiceOrder.builder()
                .id(id)
                .customerId(1L)
                .customerName("Test Customer")
                .vehicleId(1L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status(status)
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(status.isInExecution() ? LocalDateTime.now() : null)
                .finishedAt(null)
                .deliveredAt(null)
                .services(List.of())
                .resources(List.of())
                .build();
    }

    @Test
    @DisplayName("Should cancel order in RECEIVED status")
    void shouldCancelOrderInReceivedStatus() {
        // Arrange
        Long orderId = 1L;
        ServiceOrder existingOrder = createOrderWithStatus(orderId, ServiceOrderStatus.received());
        String cancellationReason = "Customer request";

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, cancellationReason);

        // Assert
        assertNotNull(result);
        assertEquals("CANCELLED", result.status().value());
        verify(eventPublisher).publishOrderCancelled(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Should cancel order in IN_DIAGNOSIS status")
    void shouldCancelOrderInDiagnosisStatus() {
        // Arrange
        Long orderId = 2L;
        ServiceOrder existingOrder = createOrderWithStatus(orderId, ServiceOrderStatus.inDiagnosis());
        String cancellationReason = "Parts unavailable";

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, cancellationReason);

        // Assert
        assertEquals("CANCELLED", result.status().value());
    }

    @Test
    @DisplayName("Should cancel order in WAITING_APPROVAL status")
    void shouldCancelOrderInWaitingApprovalStatus() {
        // Arrange
        Long orderId = 3L;
        ServiceOrder existingOrder = createOrderWithStatus(orderId, ServiceOrderStatus.waitingApproval());
        String cancellationReason = "Budget rejected";

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, cancellationReason);

        // Assert
        assertEquals("CANCELLED", result.status().value());
    }

    @Test
    @DisplayName("Should throw InvalidDataException when order in IN_EXECUTION status")
    void shouldThrowInvalidDataExceptionWhenOrderInExecutionStatus() {
        // Arrange
        Long orderId = 4L;
        ServiceOrder existingOrder = createOrderWithStatus(orderId, ServiceOrderStatus.inExecution());
        String cancellationReason = "Payment failed";

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(InvalidDataException.class,
                () -> useCase.execute(orderId, cancellationReason));
    }

    @Test
    @DisplayName("Should throw NotFoundException when order not found")
    void shouldThrowNotFoundExceptionWhenOrderNotFound() {
        // Arrange
        Long orderId = 999L;
        when(gateway.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> useCase.execute(orderId, "Cancellation reason"));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when order already finished")
    void shouldThrowInvalidDataExceptionWhenOrderAlreadyFinished() {
        // Arrange
        Long orderId = 5L;
        ServiceOrder existingOrder = ServiceOrder.builder()
                .id(orderId)
                .customerId(1L)
                .customerName("Test Customer")
                .vehicleId(1L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status(ServiceOrderStatus.finished())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(LocalDateTime.now().minusHours(2))
                .finishedAt(LocalDateTime.now())
                .deliveredAt(null)
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(InvalidDataException.class,
                () -> useCase.execute(orderId, "Cancellation reason"));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when order already delivered")
    void shouldThrowInvalidDataExceptionWhenOrderAlreadyDelivered() {
        // Arrange
        Long orderId = 6L;
        ServiceOrder existingOrder = ServiceOrder.builder()
                .id(orderId)
                .customerId(1L)
                .customerName("Test Customer")
                .vehicleId(1L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status(ServiceOrderStatus.delivered())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(null)
                .approvedAt(LocalDateTime.now().minusHours(5))
                .finishedAt(LocalDateTime.now().minusHours(2))
                .deliveredAt(LocalDateTime.now())
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(InvalidDataException.class,
                () -> useCase.execute(orderId, "Cancellation reason"));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when order already cancelled")
    void shouldThrowInvalidDataExceptionWhenOrderAlreadyCancelled() {
        // Arrange
        Long orderId = 7L;
        ServiceOrder existingOrder = ServiceOrder.builder()
                .id(orderId)
                .customerId(1L)
                .customerName("Test Customer")
                .vehicleId(1L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
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

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(InvalidDataException.class,
                () -> useCase.execute(orderId, "Cancellation reason"));
    }
}
