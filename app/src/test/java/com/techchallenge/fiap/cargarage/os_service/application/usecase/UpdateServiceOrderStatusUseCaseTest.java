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

import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusUpdateDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderStatusUseCase;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging.ServiceOrderEventPublisher;

@ExtendWith(MockitoExtension.class)
class UpdateServiceOrderStatusUseCaseTest {

    @Mock
    private ServiceOrderGateway gateway;

    @Mock
    private ServiceOrderEventPublisher eventPublisher;

    private UpdateServiceOrderStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateServiceOrderStatusUseCase(gateway, eventPublisher);
    }

    private ServiceOrder createOrderWithStatus(Long id, ServiceOrderStatus status) {
        return ServiceOrder.builder()
                .id(id)
                .customerId(1L)
                .customerName("John Doe")
                .vehicleId(2L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Civic")
                .vehicleBrand("Honda")
                .description("Sample complaint")
                .status(status)
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(status.isInExecution() ? LocalDateTime.now() : null)
                .services(List.of())
                .resources(List.of())
                .build();
    }

    @Test
    @DisplayName("Should update status from RECEIVED to IN_DIAGNOSIS")
    void shouldUpdateStatusFromReceivedToInDiagnosis() {
        // Arrange
        Long orderId = 100L;
        ServiceOrder existingOrder = createOrderWithStatus(orderId, ServiceOrderStatus.received());
        ServiceOrderStatusUpdateDto updateDto = new ServiceOrderStatusUpdateDto("IN_DIAGNOSIS");

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(ServiceOrderStatus.inDiagnosis().value(), result.status().value());
    }

    @Test
    @DisplayName("Should update status from IN_DIAGNOSIS to WAITING_APPROVAL")
    void shouldUpdateStatusFromInDiagnosisToWaitingApproval() {
        // Arrange
        Long orderId = 100L;
        ServiceOrder existingOrder = createOrderWithStatus(orderId, ServiceOrderStatus.inDiagnosis());
        ServiceOrderStatusUpdateDto updateDto = new ServiceOrderStatusUpdateDto("WAITING_APPROVAL");

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, updateDto);

        // Assert
        assertEquals(ServiceOrderStatus.waitingApproval().value(), result.status().value());
        verify(eventPublisher).publishOrderWaitingApproval(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Should set approvedAt when transitioning to IN_EXECUTION")
    void shouldSetApprovedAtWhenTransitioningToInExecution() {
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
                .status(ServiceOrderStatus.waitingApproval())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .services(List.of())
                .resources(List.of())
                .build();
        ServiceOrderStatusUpdateDto updateDto = new ServiceOrderStatusUpdateDto("IN_EXECUTION");

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, updateDto);

        // Assert
        assertEquals(ServiceOrderStatus.inExecution().value(), result.status().value());
        assertNotNull(result.approvedAt());
    }

    @Test
    @DisplayName("Should set finishedAt when transitioning to FINISHED")
    void shouldSetFinishedAtWhenTransitioningToFinished() {
        // Arrange
        Long orderId = 100L;
        ServiceOrder existingOrder = createOrderWithStatus(orderId, ServiceOrderStatus.inExecution());
        ServiceOrderStatusUpdateDto updateDto = new ServiceOrderStatusUpdateDto("FINISHED");

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(orderId, updateDto);

        // Assert
        assertEquals(ServiceOrderStatus.finished().value(), result.status().value());
        assertNotNull(result.finishedAt());
        verify(eventPublisher).publishOrderFinished(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when order not found")
    void shouldThrowNotFoundExceptionWhenOrderNotFound() {
        // Arrange
        Long orderId = 999L;
        ServiceOrderStatusUpdateDto updateDto = new ServiceOrderStatusUpdateDto("IN_DIAGNOSIS");

        when(gateway.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> useCase.execute(orderId, updateDto));
    }

    @Test
    @DisplayName("Should throw InvalidDataException for invalid status transition")
    void shouldThrowInvalidDataExceptionForInvalidStatusTransition() {
        // Arrange
        Long orderId = 100L;
        ServiceOrder existingOrder = createOrderWithStatus(orderId, ServiceOrderStatus.received());
        ServiceOrderStatusUpdateDto updateDto = new ServiceOrderStatusUpdateDto("FINISHED"); // Invalid: Cannot go from
                                                                                             // RECEIVED to FINISHED

        when(gateway.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(InvalidDataException.class, () -> useCase.execute(orderId, updateDto));
    }
}
