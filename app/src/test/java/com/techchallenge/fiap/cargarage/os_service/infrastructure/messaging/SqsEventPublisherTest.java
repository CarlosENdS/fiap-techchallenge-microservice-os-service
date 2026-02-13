package com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
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

import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

@ExtendWith(MockitoExtension.class)
class SqsEventPublisherTest {

    @Mock
    private SqsClient sqsClient;

    private SqsEventPublisher sqsEventPublisher;

    private static final String TEST_QUEUE_URL = "http://localhost:4566/000000000000/os-events.fifo";

    @BeforeEach
    void setUp() throws Exception {
        sqsEventPublisher = new SqsEventPublisher(sqsClient);

        // Set the queue URL via reflection since @Value won't work in unit tests
        Field queueUrlField = SqsEventPublisher.class.getDeclaredField("osEventsQueueUrl");
        queueUrlField.setAccessible(true);
        queueUrlField.set(sqsEventPublisher, TEST_QUEUE_URL);
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
    @DisplayName("Publish Order Created Tests")
    class PublishOrderCreatedTests {

        @Test
        @DisplayName("Should successfully publish ORDER_CREATED event")
        void shouldPublishOrderCreatedEvent() {
            // Arrange
            ServiceOrder order = createTestOrder(100L, ServiceOrderStatus.received());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-123").build());

            // Act
            assertDoesNotThrow(() -> sqsEventPublisher.publishOrderCreated(order));

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            SendMessageRequest request = captor.getValue();
            assertEquals(TEST_QUEUE_URL, request.queueUrl());
            assertTrue(request.messageBody().contains("ORDER_CREATED"));
            assertTrue(request.messageBody().contains("100"));
            assertEquals("ORDER_CREATED", request.messageAttributes().get("eventType").stringValue());
            assertEquals("100", request.messageAttributes().get("orderId").stringValue());
        }
    }

    @Nested
    @DisplayName("Publish Order Waiting Approval Tests")
    class PublishOrderWaitingApprovalTests {

        @Test
        @DisplayName("Should successfully publish ORDER_WAITING_APPROVAL event")
        void shouldPublishOrderWaitingApprovalEvent() {
            // Arrange
            ServiceOrder order = createTestOrder(101L, ServiceOrderStatus.waitingApproval());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-124").build());

            // Act
            assertDoesNotThrow(() -> sqsEventPublisher.publishOrderWaitingApproval(order));

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            SendMessageRequest request = captor.getValue();
            assertTrue(request.messageBody().contains("ORDER_WAITING_APPROVAL"));
            assertEquals("ORDER_WAITING_APPROVAL", request.messageAttributes().get("eventType").stringValue());
        }
    }

    @Nested
    @DisplayName("Publish Order Approved Tests")
    class PublishOrderApprovedTests {

        @Test
        @DisplayName("Should successfully publish ORDER_APPROVED event")
        void shouldPublishOrderApprovedEvent() {
            // Arrange
            ServiceOrder order = createTestOrder(102L, ServiceOrderStatus.inExecution());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-125").build());

            // Act
            assertDoesNotThrow(() -> sqsEventPublisher.publishOrderApproved(order));

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            SendMessageRequest request = captor.getValue();
            assertTrue(request.messageBody().contains("ORDER_APPROVED"));
            assertEquals("ORDER_APPROVED", request.messageAttributes().get("eventType").stringValue());
        }
    }

    @Nested
    @DisplayName("Publish Order Rejected Tests")
    class PublishOrderRejectedTests {

        @Test
        @DisplayName("Should successfully publish ORDER_REJECTED event")
        void shouldPublishOrderRejectedEvent() {
            // Arrange
            ServiceOrder order = createTestOrder(103L, ServiceOrderStatus.cancelled());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-126").build());

            // Act
            assertDoesNotThrow(() -> sqsEventPublisher.publishOrderRejected(order));

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            SendMessageRequest request = captor.getValue();
            assertTrue(request.messageBody().contains("ORDER_REJECTED"));
            assertEquals("ORDER_REJECTED", request.messageAttributes().get("eventType").stringValue());
        }
    }

    @Nested
    @DisplayName("Publish Order Finished Tests")
    class PublishOrderFinishedTests {

        @Test
        @DisplayName("Should successfully publish ORDER_FINISHED event")
        void shouldPublishOrderFinishedEvent() {
            // Arrange
            ServiceOrder order = createTestOrder(104L, ServiceOrderStatus.finished());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-127").build());

            // Act
            assertDoesNotThrow(() -> sqsEventPublisher.publishOrderFinished(order));

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            SendMessageRequest request = captor.getValue();
            assertTrue(request.messageBody().contains("ORDER_FINISHED"));
            assertEquals("ORDER_FINISHED", request.messageAttributes().get("eventType").stringValue());
        }
    }

    @Nested
    @DisplayName("Publish Order Delivered Tests")
    class PublishOrderDeliveredTests {

        @Test
        @DisplayName("Should successfully publish ORDER_DELIVERED event")
        void shouldPublishOrderDeliveredEvent() {
            // Arrange
            ServiceOrder order = createTestOrder(105L, ServiceOrderStatus.delivered());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-128").build());

            // Act
            assertDoesNotThrow(() -> sqsEventPublisher.publishOrderDelivered(order));

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            SendMessageRequest request = captor.getValue();
            assertTrue(request.messageBody().contains("ORDER_DELIVERED"));
            assertEquals("ORDER_DELIVERED", request.messageAttributes().get("eventType").stringValue());
        }
    }

    @Nested
    @DisplayName("Publish Order Cancelled Tests")
    class PublishOrderCancelledTests {

        @Test
        @DisplayName("Should successfully publish ORDER_CANCELLED event")
        void shouldPublishOrderCancelledEvent() {
            // Arrange
            ServiceOrder order = createTestOrder(106L, ServiceOrderStatus.cancelled());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-129").build());

            // Act
            assertDoesNotThrow(() -> sqsEventPublisher.publishOrderCancelled(order));

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            SendMessageRequest request = captor.getValue();
            assertTrue(request.messageBody().contains("ORDER_CANCELLED"));
            assertEquals("ORDER_CANCELLED", request.messageAttributes().get("eventType").stringValue());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw RuntimeException when SQS fails")
        void shouldThrowRuntimeExceptionWhenSqsFails() {
            // Arrange
            ServiceOrder order = createTestOrder(107L, ServiceOrderStatus.received());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenThrow(SqsException.builder().message("SQS connection failed").build());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> sqsEventPublisher.publishOrderCreated(order));

            assertEquals("Failed to publish event to SQS", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle order with null status")
        void shouldHandleOrderWithNullStatus() {
            // Arrange
            ServiceOrder order = ServiceOrder.builder()
                    .id(108L)
                    .customerId(1L)
                    .customerName("John Doe")
                    .vehicleId(2L)
                    .vehicleLicensePlate("XYZ-9999")
                    .vehicleModel("Model S")
                    .vehicleBrand("Tesla")
                    .description("Test order without status")
                    .status(null)
                    .totalPrice(new BigDecimal("2000.00"))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .services(List.of())
                    .resources(List.of())
                    .build();

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-130").build());

            // Act
            assertDoesNotThrow(() -> sqsEventPublisher.publishOrderCreated(order));

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            SendMessageRequest request = captor.getValue();
            assertTrue(request.messageBody().contains("ORDER_CREATED"));
        }

        @Test
        @DisplayName("Should include all order details in message body")
        void shouldIncludeAllOrderDetailsInMessageBody() {
            // Arrange
            ServiceOrder order = createTestOrder(109L, ServiceOrderStatus.received());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-131").build());

            // Act
            sqsEventPublisher.publishOrderCreated(order);

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            String messageBody = captor.getValue().messageBody();
            assertTrue(messageBody.contains("109"));
            assertTrue(messageBody.contains("John Doe"));
            assertTrue(messageBody.contains("ABC-1234"));
            assertTrue(messageBody.contains("Test service order"));
        }

        @Test
        @DisplayName("Should set FIFO queue message group and deduplication IDs")
        void shouldSetFifoQueueAttributes() {
            // Arrange
            ServiceOrder order = createTestOrder(110L, ServiceOrderStatus.received());
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-132").build());

            // Act
            sqsEventPublisher.publishOrderCreated(order);

            // Assert
            ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
            verify(sqsClient).sendMessage(captor.capture());

            SendMessageRequest request = captor.getValue();
            assertEquals("os-service-events", request.messageGroupId());
            assertTrue(request.messageDeduplicationId().startsWith("110-ORDER_CREATED-"));
        }
    }
}
