package com.techchallenge.fiap.cargarage.os_service.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.techchallenge.fiap.cargarage.os_service.application.controller.ServiceOrderCleanArchController;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderApprovalDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusUpdateDto;

@WebMvcTest(ServiceOrderController.class)
class ServiceOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceOrderCleanArchController cleanArchController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private ServiceOrderDto createSampleDto(Long id) {
        return ServiceOrderDto.builder()
                .id(id)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status("RECEIVED")
                .totalPrice(new BigDecimal("500.00"))
                .services(List.of())
                .resources(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .build();
    }

    @Test
    @DisplayName("Should create service order")
    void shouldCreateServiceOrder() throws Exception {
        // Arrange
        Long orderId = 1L;
        ServiceOrderDto expectedDto = createSampleDto(orderId);

        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .services(List.of(ServiceOrderItemRequestDto.builder()
                        .serviceId(1L)
                        .serviceName("Service")
                        .serviceDescription("Service Description")
                        .price(new BigDecimal("100.00"))
                        .quantity(1)
                        .build()))
                .resources(List.of(ServiceOrderResourceRequestDto.builder()
                        .resourceId(2L)
                        .resourceName("Part")
                        .resourceDescription("Part Description")
                        .resourceType("PART")
                        .price(new BigDecimal("50.00"))
                        .quantity(1)
                        .build()))
                .build();

        when(cleanArchController.create(any(ServiceOrderRequestDto.class))).thenReturn(expectedDto);

        // Act & Assert
        mockMvc.perform(post("/service-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @DisplayName("Should get service order by ID")
    void shouldGetServiceOrderById() throws Exception {
        // Arrange
        Long orderId = 1L;
        ServiceOrderDto expectedDto = createSampleDto(orderId);

        when(cleanArchController.findById(orderId)).thenReturn(expectedDto);

        // Act & Assert
        mockMvc.perform(get("/service-orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @DisplayName("Should get all service orders")
    void shouldGetAllServiceOrders() throws Exception {
        // Arrange
        List<ServiceOrderDto> orders = List.of(
                createSampleDto(1L),
                createSampleDto(2L));
        PageDto<ServiceOrderDto> page = new PageDto<>(orders, 2, 0, 10);

        when(cleanArchController.findAll(eq(0), eq(10))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/service-orders")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Should update service order")
    void shouldUpdateServiceOrder() throws Exception {
        // Arrange
        Long orderId = 1L;
        ServiceOrderDto expectedDto = createSampleDto(orderId);

        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Updated complaint")
                .services(List.of())
                .resources(List.of())
                .build();

        when(cleanArchController.update(eq(orderId), any(ServiceOrderRequestDto.class)))
                .thenReturn(expectedDto);

        // Act & Assert
        mockMvc.perform(put("/service-orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @DisplayName("Should update service order status")
    void shouldUpdateServiceOrderStatus() throws Exception {
        // Arrange
        Long orderId = 1L;
        ServiceOrderDto expectedDto = ServiceOrderDto.builder()
                .id(orderId)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status("IN_DIAGNOSIS")
                .totalPrice(new BigDecimal("500.00"))
                .services(List.of())
                .resources(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .build();

        ServiceOrderStatusUpdateDto updateDto = ServiceOrderStatusUpdateDto.builder()
                .status("IN_DIAGNOSIS")
                .build();

        when(cleanArchController.updateStatus(eq(orderId), any(ServiceOrderStatusUpdateDto.class)))
                .thenReturn(expectedDto);

        // Act & Assert
        mockMvc.perform(put("/service-orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_DIAGNOSIS"));
    }

    @Test
    @DisplayName("Should process approval")
    void shouldProcessApproval() throws Exception {
        // Arrange
        Long orderId = 1L;
        ServiceOrderDto expectedDto = ServiceOrderDto.builder()
                .id(orderId)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status("IN_EXECUTION")
                .totalPrice(new BigDecimal("500.00"))
                .services(List.of())
                .resources(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .build();

        ServiceOrderApprovalDto approvalDto = new ServiceOrderApprovalDto(true);

        when(cleanArchController.processApproval(eq(orderId), any(ServiceOrderApprovalDto.class)))
                .thenReturn(expectedDto);

        // Act & Assert
        mockMvc.perform(put("/service-orders/{id}/approve", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_EXECUTION"));
    }

    @Test
    @DisplayName("Should cancel service order")
    void shouldCancelServiceOrder() throws Exception {
        // Arrange
        Long orderId = 1L;
        String cancellationReason = "Customer request";
        ServiceOrderDto expectedDto = ServiceOrderDto.builder()
                .id(orderId)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status("CANCELLED")
                .totalPrice(new BigDecimal("500.00"))
                .services(List.of())
                .resources(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .approvedAt(null)
                .finishedAt(null)
                .deliveredAt(null)
                .build();

        when(cleanArchController.cancel(eq(orderId), eq(cancellationReason))).thenReturn(expectedDto);

        // Act & Assert
        mockMvc.perform(delete("/service-orders/{id}", orderId)
                .param("reason", cancellationReason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
