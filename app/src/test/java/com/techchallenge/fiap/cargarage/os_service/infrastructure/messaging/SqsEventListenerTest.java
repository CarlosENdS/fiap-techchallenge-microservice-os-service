package com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusUpdateDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.enums.ServiceOrderStatusEnum;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CancelServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderStatusUseCase;

@ExtendWith(MockitoExtension.class)
class SqsEventListenerTest {

    @Mock
    private UpdateServiceOrderStatusUseCase updateStatusUseCase;

    @Mock
    private CancelServiceOrderUseCase cancelUseCase;

    private SqsEventListener sqsEventListener;

    @BeforeEach
    void setUp() {
        sqsEventListener = new SqsEventListener(updateStatusUseCase, cancelUseCase);
    }

    private ServiceOrder createTestOrder(Long id, ServiceOrderStatus status) {
        return ServiceOrder.builder()
                .id(id)
                .customerId(1L)
                .customerName("John Doe")
                .vehicleId(2L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Civic")
                .vehicleBrand("Honda")
                .description("Test service order")
                .status(status)
                .totalPrice(new BigDecimal("1500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .services(List.of())
                .resources(List.of())
                .build();
    }

    @Nested
    @DisplayName("Handle Quote Approved Tests")
    class HandleQuoteApprovedTests {

        @Test
        @DisplayName("Should process quote approved event successfully")
        void shouldProcessQuoteApprovedEventSuccessfully() {
            // Arrange
            String message = "{\"orderId\": 100}";
            ServiceOrder updatedOrder = createTestOrder(100L, ServiceOrderStatus.waitingApproval());

            when(updateStatusUseCase.execute(eq(100L), any(ServiceOrderStatusUpdateDto.class)))
                    .thenReturn(updatedOrder);

            // Act
            assertDoesNotThrow(() -> sqsEventListener.handleQuoteApproved(message));

            // Assert
            ArgumentCaptor<ServiceOrderStatusUpdateDto> captor = ArgumentCaptor
                    .forClass(ServiceOrderStatusUpdateDto.class);
            verify(updateStatusUseCase).execute(eq(100L), captor.capture());

            ServiceOrderStatusUpdateDto updateDto = captor.getValue();
            assertEquals(ServiceOrderStatusEnum.WAITING_APPROVAL.name(), updateDto.status());
        }

        @Test
        @DisplayName("Should throw RuntimeException for invalid JSON")
        void shouldThrowRuntimeExceptionForInvalidJson() {
            // Arrange
            String invalidMessage = "invalid json";

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> sqsEventListener.handleQuoteApproved(invalidMessage));

            assertEquals("Failed to process quote approved event", exception.getMessage());
        }

        @Test
        @DisplayName("Should rethrow exception when use case fails")
        void shouldRethrowExceptionWhenUseCaseFails() {
            // Arrange
            String message = "{\"orderId\": 999}";
            when(updateStatusUseCase.execute(eq(999L), any(ServiceOrderStatusUpdateDto.class)))
                    .thenThrow(new NotFoundException("Order not found"));

            // Act & Assert
            assertThrows(NotFoundException.class,
                    () -> sqsEventListener.handleQuoteApproved(message));
        }
    }

    @Nested
    @DisplayName("Handle Execution Completed Tests")
    class HandleExecutionCompletedTests {

        @Test
        @DisplayName("Should process execution completed event successfully")
        void shouldProcessExecutionCompletedEventSuccessfully() {
            // Arrange
            String message = "{\"orderId\": 101}";
            ServiceOrder updatedOrder = createTestOrder(101L, ServiceOrderStatus.finished());

            when(updateStatusUseCase.execute(eq(101L), any(ServiceOrderStatusUpdateDto.class)))
                    .thenReturn(updatedOrder);

            // Act
            assertDoesNotThrow(() -> sqsEventListener.handleExecutionCompleted(message));

            // Assert
            ArgumentCaptor<ServiceOrderStatusUpdateDto> captor = ArgumentCaptor
                    .forClass(ServiceOrderStatusUpdateDto.class);
            verify(updateStatusUseCase).execute(eq(101L), captor.capture());

            ServiceOrderStatusUpdateDto updateDto = captor.getValue();
            assertEquals(ServiceOrderStatusEnum.FINISHED.name(), updateDto.status());
        }

        @Test
        @DisplayName("Should throw RuntimeException for invalid JSON")
        void shouldThrowRuntimeExceptionForInvalidJson() {
            // Arrange
            String invalidMessage = "{ broken json";

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> sqsEventListener.handleExecutionCompleted(invalidMessage));

            assertEquals("Failed to process execution completed event", exception.getMessage());
        }

        @Test
        @DisplayName("Should rethrow exception when order not found")
        void shouldRethrowExceptionWhenOrderNotFound() {
            // Arrange
            String message = "{\"orderId\": 888}";
            when(updateStatusUseCase.execute(eq(888L), any(ServiceOrderStatusUpdateDto.class)))
                    .thenThrow(new NotFoundException("Order not found with id: 888"));

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> sqsEventListener.handleExecutionCompleted(message));

            assertTrue(exception.getMessage().contains("888"));
        }
    }

    @Nested
    @DisplayName("Handle Payment Failed Tests")
    class HandlePaymentFailedTests {

        @Test
        @DisplayName("Should process payment failed event with reason")
        void shouldProcessPaymentFailedEventWithReason() {
            // Arrange
            String message = "{\"orderId\": 102, \"reason\": \"Insufficient funds\"}";
            ServiceOrder cancelledOrder = createTestOrder(102L, ServiceOrderStatus.cancelled());

            when(cancelUseCase.execute(eq(102L), eq("Insufficient funds")))
                    .thenReturn(cancelledOrder);

            // Act
            assertDoesNotThrow(() -> sqsEventListener.handlePaymentFailed(message));

            // Assert
            verify(cancelUseCase).execute(102L, "Insufficient funds");
        }

        @Test
        @DisplayName("Should process payment failed event with default reason")
        void shouldProcessPaymentFailedEventWithDefaultReason() {
            // Arrange
            String message = "{\"orderId\": 103}";
            ServiceOrder cancelledOrder = createTestOrder(103L, ServiceOrderStatus.cancelled());

            when(cancelUseCase.execute(eq(103L), eq("Payment failed")))
                    .thenReturn(cancelledOrder);

            // Act
            assertDoesNotThrow(() -> sqsEventListener.handlePaymentFailed(message));

            // Assert
            verify(cancelUseCase).execute(103L, "Payment failed");
        }

        @Test
        @DisplayName("Should throw RuntimeException for invalid JSON")
        void shouldThrowRuntimeExceptionForInvalidJson() {
            // Arrange
            String invalidMessage = "{orderId: 'abc'}"; // Invalid JSON

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> sqsEventListener.handlePaymentFailed(invalidMessage));

            assertEquals("Failed to process payment failed event", exception.getMessage());
        }

        @Test
        @DisplayName("Should rethrow exception when cancel fails")
        void shouldRethrowExceptionWhenCancelFails() {
            // Arrange
            String message = "{\"orderId\": 777}";
            when(cancelUseCase.execute(eq(777L), any(String.class)))
                    .thenThrow(new NotFoundException("Order not found"));

            // Act & Assert
            assertThrows(NotFoundException.class,
                    () -> sqsEventListener.handlePaymentFailed(message));
        }
    }

    @Nested
    @DisplayName("Handle Resource Unavailable Tests")
    class HandleResourceUnavailableTests {

        @Test
        @DisplayName("Should process resource unavailable event with reason")
        void shouldProcessResourceUnavailableEventWithReason() {
            // Arrange
            String message = "{\"orderId\": 104, \"reason\": \"Part out of stock\"}";
            ServiceOrder cancelledOrder = createTestOrder(104L, ServiceOrderStatus.cancelled());

            when(cancelUseCase.execute(eq(104L), eq("Part out of stock")))
                    .thenReturn(cancelledOrder);

            // Act
            assertDoesNotThrow(() -> sqsEventListener.handleResourceUnavailable(message));

            // Assert
            verify(cancelUseCase).execute(104L, "Part out of stock");
        }

        @Test
        @DisplayName("Should process resource unavailable event with default reason")
        void shouldProcessResourceUnavailableEventWithDefaultReason() {
            // Arrange
            String message = "{\"orderId\": 105}";
            ServiceOrder cancelledOrder = createTestOrder(105L, ServiceOrderStatus.cancelled());

            when(cancelUseCase.execute(eq(105L), eq("Resource unavailable")))
                    .thenReturn(cancelledOrder);

            // Act
            assertDoesNotThrow(() -> sqsEventListener.handleResourceUnavailable(message));

            // Assert
            verify(cancelUseCase).execute(105L, "Resource unavailable");
        }

        @Test
        @DisplayName("Should throw RuntimeException for invalid JSON")
        void shouldThrowRuntimeExceptionForInvalidJson() {
            // Arrange - completely invalid JSON that can't be parsed
            String invalidMessage = "not json at all {{{";

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> sqsEventListener.handleResourceUnavailable(invalidMessage));

            assertEquals("Failed to process resource unavailable event", exception.getMessage());
        }

        @Test
        @DisplayName("Should rethrow exception when use case throws")
        void shouldRethrowExceptionWhenUseCaseThrows() {
            // Arrange
            String message = "{\"orderId\": 666}";
            when(cancelUseCase.execute(eq(666L), any(String.class)))
                    .thenThrow(new IllegalStateException("Cannot cancel order"));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> sqsEventListener.handleResourceUnavailable(message));
        }
    }

    @Nested
    @DisplayName("Message Parsing Tests")
    class MessageParsingTests {

        @Test
        @DisplayName("Should handle message with additional fields")
        void shouldHandleMessageWithAdditionalFields() {
            // Arrange
            String message = "{\"orderId\": 200, \"extraField\": \"ignored\", \"anotherField\": 123}";
            ServiceOrder updatedOrder = createTestOrder(200L, ServiceOrderStatus.waitingApproval());

            when(updateStatusUseCase.execute(eq(200L), any(ServiceOrderStatusUpdateDto.class)))
                    .thenReturn(updatedOrder);

            // Act
            assertDoesNotThrow(() -> sqsEventListener.handleQuoteApproved(message));

            // Assert
            verify(updateStatusUseCase).execute(eq(200L), any(ServiceOrderStatusUpdateDto.class));
        }

        @Test
        @DisplayName("Should handle message with orderId as string")
        void shouldHandleMessageWithOrderIdAsString() {
            // Arrange
            String message = "{\"orderId\": \"201\"}";
            ServiceOrder updatedOrder = createTestOrder(201L, ServiceOrderStatus.waitingApproval());

            when(updateStatusUseCase.execute(eq(201L), any(ServiceOrderStatusUpdateDto.class)))
                    .thenReturn(updatedOrder);

            // Act
            assertDoesNotThrow(() -> sqsEventListener.handleQuoteApproved(message));

            // Assert
            verify(updateStatusUseCase).execute(eq(201L), any(ServiceOrderStatusUpdateDto.class));
        }
    }
}
