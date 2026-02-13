package com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusUpdateDto;
import com.techchallenge.fiap.cargarage.os_service.application.enums.ServiceOrderStatusEnum;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CancelServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderStatusUseCase;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;

/**
 * AWS SQS message listener for consuming events from other services.
 * Part of the Saga pattern implementation - handles compensation events.
 */
@Slf4j
@Component
public class SqsEventListener {

    private final UpdateServiceOrderStatusUseCase updateStatusUseCase;
    private final CancelServiceOrderUseCase cancelUseCase;
    private final ObjectMapper objectMapper;

    public SqsEventListener(
            UpdateServiceOrderStatusUseCase updateStatusUseCase,
            CancelServiceOrderUseCase cancelUseCase) {
        this.updateStatusUseCase = updateStatusUseCase;
        this.cancelUseCase = cancelUseCase;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Handles quote approved event from Billing service.
     * Transitions order to IN_EXECUTION status.
     */
    @SqsListener("${messaging.sqs.queue.quote-approved}")
    public void handleQuoteApproved(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            Long orderId = json.get("orderId").asLong();

            log.info("Received quote approved event for order: {}", orderId);

            updateStatusUseCase.execute(orderId,
                    ServiceOrderStatusUpdateDto.builder()
                            .status(ServiceOrderStatusEnum.IN_EXECUTION.name())
                            .build());
        } catch (JsonProcessingException e) {
            log.error("Error processing quote approved event", e);
            throw new RuntimeException("Failed to process quote approved event", e);
        } catch (Exception e) {
            log.error("Error handling quote approved event", e);
            throw e;
        }
    }

    /**
     * Handles execution completed event from Execution service.
     * Transitions order to FINISHED status.
     */
    @SqsListener("${messaging.sqs.queue.execution-completed}")
    public void handleExecutionCompleted(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            Long orderId = json.get("orderId").asLong();

            log.info("Received execution completed event for order: {}", orderId);

            updateStatusUseCase.execute(orderId,
                    ServiceOrderStatusUpdateDto.builder()
                            .status(ServiceOrderStatusEnum.FINISHED.name())
                            .build());
        } catch (JsonProcessingException e) {
            log.error("Error processing execution completed event", e);
            throw new RuntimeException("Failed to process execution completed event", e);
        } catch (Exception e) {
            log.error("Error handling execution completed event", e);
            throw e;
        }
    }

    /**
     * Handles payment failed event - Saga compensation.
     * Cancels the order.
     */
    @SqsListener("${messaging.sqs.queue.payment-failed}")
    public void handlePaymentFailed(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            Long orderId = json.get("orderId").asLong();
            String reason = json.has("reason") ? json.get("reason").asText() : "Payment failed";

            log.info("Received payment failed event for order: {}. Compensating...", orderId);

            cancelUseCase.execute(orderId, reason);
        } catch (JsonProcessingException e) {
            log.error("Error processing payment failed event", e);
            throw new RuntimeException("Failed to process payment failed event", e);
        } catch (Exception e) {
            log.error("Error handling payment failed event", e);
            throw e;
        }
    }

    /**
     * Handles resource unavailable event - Saga compensation.
     * Cancels the order.
     */
    @SqsListener("${messaging.sqs.queue.resource-unavailable}")
    public void handleResourceUnavailable(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            Long orderId = json.get("orderId").asLong();
            String reason = json.has("reason") ? json.get("reason").asText() : "Resource unavailable";

            log.info("Received resource unavailable event for order: {}. Compensating...", orderId);

            cancelUseCase.execute(orderId, reason);
        } catch (JsonProcessingException e) {
            log.error("Error processing resource unavailable event", e);
            throw new RuntimeException("Failed to process resource unavailable event", e);
        } catch (Exception e) {
            log.error("Error handling resource unavailable event", e);
            throw e;
        }
    }
}
