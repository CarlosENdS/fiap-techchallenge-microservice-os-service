package com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Value("${messaging.sqs.queue.billing-order-events-url:}")
    private String billingOrderEventsQueueUrl;

    public SqsEventPublisher(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void publishOrderCreated(ServiceOrder order) {
        publishEvent("ORDER_CREATED", order);
        publishOrderToBillingQueue(order);
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

    /**
     * Publishes ORDER_CREATED to the Billing Service standard queue.
     * Payload is adapted so the billing-service consumer can create a Budget.
     */
    private void publishOrderToBillingQueue(ServiceOrder order) {
        if (billingOrderEventsQueueUrl == null || billingOrderEventsQueueUrl.isBlank()) {
            log.debug("Billing queue URL not configured, skipping billing notification");
            return;
        }
        try {
            Map<String, Object> billingPayload = new HashMap<>();
            billingPayload.put("eventType", "ORDER_CREATED");
            billingPayload.put("orderId", order.id());
            billingPayload.put("serviceOrderId", String.valueOf(order.id()));
            billingPayload.put("customerId", String.valueOf(order.customerId()));
            billingPayload.put("vehicleId", String.valueOf(order.vehicleId()));
            billingPayload.put("customerName", order.customerName());
            billingPayload.put("vehicleLicensePlate", order.vehicleLicensePlate());
            billingPayload.put("description", order.description());
            billingPayload.put("status", order.status() != null ? order.status().value() : null);
            billingPayload.put("totalPrice", order.totalPrice() != null ? order.totalPrice().toString() : "0");
            billingPayload.put("items", buildBillingItems(order));
            billingPayload.put("timestamp", LocalDateTime.now().toString());

            String messageBody = objectMapper.writeValueAsString(billingPayload);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(billingOrderEventsQueueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(request);
            log.info("Published ORDER_CREATED to billing queue for order: {} with {} item(s)",
                    order.id(), billingPayload.get("items") != null ?
                    ((List<?>) billingPayload.get("items")).size() : 0);
        } catch (Exception e) {
            log.error("Error publishing to billing queue for order: {}", order.id(), e);
        }
    }

    /**
     * Builds the items array for the Billing payload from ServiceOrder services + resources.
     * Each item follows the Billing contract: {type, itemCode, description, quantity, unitPrice}.
     */
    private List<Map<String, Object>> buildBillingItems(ServiceOrder order) {
        List<Map<String, Object>> items = new ArrayList<>();

        if (order.services() != null) {
            for (var service : order.services()) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "SERVICE");
                item.put("itemCode", String.valueOf(service.serviceId()));
                item.put("description", service.serviceDescription());
                item.put("quantity", service.quantity());
                item.put("unitPrice", service.price() != null ? service.price().toString() : "0");
                items.add(item);
            }
        }

        if (order.resources() != null) {
            for (var resource : order.resources()) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "RESOURCE");
                item.put("itemCode", String.valueOf(resource.resourceId()));
                item.put("description", resource.resourceDescription());
                item.put("quantity", resource.quantity());
                item.put("unitPrice", resource.price() != null ? resource.price().toString() : "0");
                items.add(item);
            }
        }

        return items;
    }
}
