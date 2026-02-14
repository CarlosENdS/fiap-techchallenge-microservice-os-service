package com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * AWS SQS implementation of ServiceOrderEventPublisher.
 * Publishes events to SQS queues for Saga pattern integration.
 */
@Slf4j
@Component
public class SqsEventPublisher implements ServiceOrderEventPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${messaging.sqs.queue.os-events-url}")
    private String osEventsQueueUrl;

    public SqsEventPublisher(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void publishOrderCreated(ServiceOrder order) {
        publishEvent("ORDER_CREATED", order);
    }

    @Override
    public void publishOrderWaitingApproval(ServiceOrder order) {
        publishEvent("ORDER_WAITING_APPROVAL", order);
    }

    @Override
    public void publishOrderApproved(ServiceOrder order) {
        publishEvent("ORDER_APPROVED", order);
    }

    @Override
    public void publishOrderRejected(ServiceOrder order) {
        publishEvent("ORDER_REJECTED", order);
    }

    @Override
    public void publishOrderFinished(ServiceOrder order) {
        publishEvent("ORDER_FINISHED", order);
    }

    @Override
    public void publishOrderDelivered(ServiceOrder order) {
        publishEvent("ORDER_DELIVERED", order);
    }

    @Override
    public void publishOrderCancelled(ServiceOrder order) {
        publishEvent("ORDER_CANCELLED", order);
    }

    private void publishEvent(String eventType, ServiceOrder order) {
        try {
            ServiceOrderEventDto event = ServiceOrderEventDto.builder()
                    .eventType(eventType)
                    .orderId(order.id())
                    .customerId(order.customerId())
                    .customerName(order.customerName())
                    .vehicleId(order.vehicleId())
                    .vehicleLicensePlate(order.vehicleLicensePlate())
                    .status(order.status() != null ? order.status().value() : null)
                    .description(order.description())
                    .timestamp(LocalDateTime.now())
                    .build();

            String messageBody = objectMapper.writeValueAsString(event);

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("eventType", MessageAttributeValue.builder()
                    .stringValue(eventType)
                    .dataType("String")
                    .build());
            messageAttributes.put("orderId", MessageAttributeValue.builder()
                    .stringValue(order.id().toString())
                    .dataType("String")
                    .build());

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(osEventsQueueUrl)
                    .messageBody(messageBody)
                    .messageAttributes(messageAttributes)
                    .messageGroupId("os-service-events") // For FIFO queues
                    .messageDeduplicationId(order.id() + "-" + eventType + "-" + System.currentTimeMillis())
                    .build();

            sqsClient.sendMessage(sendMessageRequest);

            log.info("Published SQS event: {} for order: {}", eventType, order.id());
        } catch (JsonProcessingException e) {
            log.error("Error serializing event for order: {}", order.id(), e);
            throw new RuntimeException("Failed to serialize event", e);
        } catch (Exception e) {
            log.error("Error publishing event to SQS for order: {}", order.id(), e);
            throw new RuntimeException("Failed to publish event to SQS", e);
        }
    }
}
