package com.techchallenge.fiap.cargarage.os_service.application.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DtoTest {

    @Nested
    @DisplayName("ServiceOrderDto Tests")
    class ServiceOrderDtoTests {

        @Test
        @DisplayName("Should create ServiceOrderDto using builder")
        void shouldCreateServiceOrderDtoUsingBuilder() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<ServiceOrderItemDto> services = List.of(
                    ServiceOrderItemDto.builder()
                            .id(1L)
                            .serviceId(10L)
                            .serviceName("Oil Change")
                            .price(new BigDecimal("50.00"))
                            .quantity(1)
                            .totalPrice(new BigDecimal("50.00"))
                            .build());
            List<ServiceOrderResourceDto> resources = List.of(
                    ServiceOrderResourceDto.builder()
                            .id(1L)
                            .resourceId(20L)
                            .resourceName("Oil Filter")
                            .price(new BigDecimal("25.00"))
                            .quantity(1)
                            .totalPrice(new BigDecimal("25.00"))
                            .build());

            // Act
            ServiceOrderDto dto = ServiceOrderDto.builder()
                    .id(100L)
                    .customerId(1L)
                    .customerName("John Doe")
                    .vehicleId(2L)
                    .vehicleLicensePlate("ABC-1234")
                    .vehicleModel("Civic")
                    .vehicleBrand("Honda")
                    .description("Regular maintenance")
                    .status("RECEIVED")
                    .totalPrice(new BigDecimal("75.00"))
                    .createdAt(now)
                    .updatedAt(now)
                    .approvedAt(null)
                    .finishedAt(null)
                    .deliveredAt(null)
                    .services(services)
                    .resources(resources)
                    .build();

            // Assert
            assertNotNull(dto);
            assertEquals(100L, dto.id());
            assertEquals(1L, dto.customerId());
            assertEquals("John Doe", dto.customerName());
            assertEquals(2L, dto.vehicleId());
            assertEquals("ABC-1234", dto.vehicleLicensePlate());
            assertEquals("Civic", dto.vehicleModel());
            assertEquals("Honda", dto.vehicleBrand());
            assertEquals("Regular maintenance", dto.description());
            assertEquals("RECEIVED", dto.status());
            assertEquals(new BigDecimal("75.00"), dto.totalPrice());
            assertEquals(now, dto.createdAt());
            assertEquals(1, dto.services().size());
            assertEquals(1, dto.resources().size());
        }

        @Test
        @DisplayName("Should handle null collections")
        void shouldHandleNullCollections() {
            // Act
            ServiceOrderDto dto = ServiceOrderDto.builder()
                    .id(1L)
                    .customerId(1L)
                    .vehicleId(1L)
                    .services(null)
                    .resources(null)
                    .build();

            // Assert
            assertNull(dto.services());
            assertNull(dto.resources());
        }

        @Test
        @DisplayName("Should handle empty collections")
        void shouldHandleEmptyCollections() {
            // Act
            ServiceOrderDto dto = ServiceOrderDto.builder()
                    .id(1L)
                    .customerId(1L)
                    .vehicleId(1L)
                    .services(List.of())
                    .resources(List.of())
                    .build();

            // Assert
            assertNotNull(dto.services());
            assertNotNull(dto.resources());
            assertTrue(dto.services().isEmpty());
            assertTrue(dto.resources().isEmpty());
        }
    }

    @Nested
    @DisplayName("ServiceOrderRequestDto Tests")
    class ServiceOrderRequestDtoTests {

        @Test
        @DisplayName("Should create ServiceOrderRequestDto using builder")
        void shouldCreateServiceOrderRequestDtoUsingBuilder() {
            // Arrange
            List<ServiceOrderItemRequestDto> services = List.of();
            List<ServiceOrderResourceRequestDto> resources = List.of();

            // Act
            ServiceOrderRequestDto dto = ServiceOrderRequestDto.builder()
                    .customerId(1L)
                    .customerName("Jane Smith")
                    .vehicleId(2L)
                    .vehicleLicensePlate("XYZ-9876")
                    .vehicleModel("Model S")
                    .vehicleBrand("Tesla")
                    .description("Battery check")
                    .services(services)
                    .resources(resources)
                    .build();

            // Assert
            assertNotNull(dto);
            assertEquals(1L, dto.customerId());
            assertEquals("Jane Smith", dto.customerName());
            assertEquals(2L, dto.vehicleId());
            assertEquals("XYZ-9876", dto.vehicleLicensePlate());
            assertEquals("Model S", dto.vehicleModel());
            assertEquals("Tesla", dto.vehicleBrand());
            assertEquals("Battery check", dto.description());
        }

        @Test
        @DisplayName("Should handle required fields only")
        void shouldHandleRequiredFieldsOnly() {
            // Act
            ServiceOrderRequestDto dto = ServiceOrderRequestDto.builder()
                    .customerId(100L)
                    .vehicleId(200L)
                    .build();

            // Assert
            assertEquals(100L, dto.customerId());
            assertEquals(200L, dto.vehicleId());
            assertNull(dto.customerName());
            assertNull(dto.description());
        }
    }

    @Nested
    @DisplayName("ServiceOrderApprovalDto Tests")
    class ServiceOrderApprovalDtoTests {

        @Test
        @DisplayName("Should create approved ServiceOrderApprovalDto")
        void shouldCreateApprovedServiceOrderApprovalDto() {
            // Act
            ServiceOrderApprovalDto dto = ServiceOrderApprovalDto.builder()
                    .approved(true)
                    .build();

            // Assert
            assertTrue(dto.approved());
        }

        @Test
        @DisplayName("Should create rejected ServiceOrderApprovalDto")
        void shouldCreateRejectedServiceOrderApprovalDto() {
            // Act
            ServiceOrderApprovalDto dto = ServiceOrderApprovalDto.builder()
                    .approved(false)
                    .build();

            // Assert
            assertFalse(dto.approved());
        }

        @Test
        @DisplayName("Should default to false when not specified")
        void shouldDefaultToFalseWhenNotSpecified() {
            // Act
            ServiceOrderApprovalDto dto = ServiceOrderApprovalDto.builder().build();

            // Assert
            assertFalse(dto.approved());
        }
    }

    @Nested
    @DisplayName("ServiceOrderItemDto Tests")
    class ServiceOrderItemDtoTests {

        @Test
        @DisplayName("Should create ServiceOrderItemDto using builder")
        void shouldCreateServiceOrderItemDtoUsingBuilder() {
            // Act
            ServiceOrderItemDto dto = ServiceOrderItemDto.builder()
                    .id(1L)
                    .serviceId(10L)
                    .serviceName("Brake Inspection")
                    .serviceDescription("Full brake system inspection")
                    .price(new BigDecimal("100.00"))
                    .quantity(2)
                    .totalPrice(new BigDecimal("200.00"))
                    .build();

            // Assert
            assertNotNull(dto);
            assertEquals(1L, dto.id());
            assertEquals(10L, dto.serviceId());
            assertEquals("Brake Inspection", dto.serviceName());
            assertEquals("Full brake system inspection", dto.serviceDescription());
            assertEquals(new BigDecimal("100.00"), dto.price());
            assertEquals(2, dto.quantity());
            assertEquals(new BigDecimal("200.00"), dto.totalPrice());
        }

        @Test
        @DisplayName("Should handle decimal prices with precision")
        void shouldHandleDecimalPricesWithPrecision() {
            // Act
            ServiceOrderItemDto dto = ServiceOrderItemDto.builder()
                    .id(1L)
                    .serviceId(10L)
                    .price(new BigDecimal("99.99"))
                    .quantity(3)
                    .totalPrice(new BigDecimal("299.97"))
                    .build();

            // Assert
            assertEquals(new BigDecimal("99.99"), dto.price());
            assertEquals(new BigDecimal("299.97"), dto.totalPrice());
        }
    }

    @Nested
    @DisplayName("ServiceOrderResourceDto Tests")
    class ServiceOrderResourceDtoTests {

        @Test
        @DisplayName("Should create ServiceOrderResourceDto using builder")
        void shouldCreateServiceOrderResourceDtoUsingBuilder() {
            // Act
            ServiceOrderResourceDto dto = ServiceOrderResourceDto.builder()
                    .id(1L)
                    .resourceId(20L)
                    .resourceName("Brake Pad")
                    .resourceDescription("High performance brake pad")
                    .resourceType("PART")
                    .price(new BigDecimal("150.00"))
                    .quantity(4)
                    .totalPrice(new BigDecimal("600.00"))
                    .build();

            // Assert
            assertNotNull(dto);
            assertEquals(1L, dto.id());
            assertEquals(20L, dto.resourceId());
            assertEquals("Brake Pad", dto.resourceName());
            assertEquals("High performance brake pad", dto.resourceDescription());
            assertEquals("PART", dto.resourceType());
            assertEquals(new BigDecimal("150.00"), dto.price());
            assertEquals(4, dto.quantity());
            assertEquals(new BigDecimal("600.00"), dto.totalPrice());
        }

        @Test
        @DisplayName("Should handle different resource types")
        void shouldHandleDifferentResourceTypes() {
            // Act
            ServiceOrderResourceDto partDto = ServiceOrderResourceDto.builder()
                    .id(1L)
                    .resourceId(1L)
                    .resourceType("PART")
                    .build();

            ServiceOrderResourceDto supplyDto = ServiceOrderResourceDto.builder()
                    .id(2L)
                    .resourceId(2L)
                    .resourceType("SUPPLY")
                    .build();

            // Assert
            assertEquals("PART", partDto.resourceType());
            assertEquals("SUPPLY", supplyDto.resourceType());
        }
    }

    @Nested
    @DisplayName("ServiceOrderStatusUpdateDto Tests")
    class ServiceOrderStatusUpdateDtoTests {

        @Test
        @DisplayName("Should create ServiceOrderStatusUpdateDto using builder")
        void shouldCreateServiceOrderStatusUpdateDtoUsingBuilder() {
            // Act
            ServiceOrderStatusUpdateDto dto = ServiceOrderStatusUpdateDto.builder()
                    .status("IN_EXECUTION")
                    .build();

            // Assert
            assertNotNull(dto);
            assertEquals("IN_EXECUTION", dto.status());
        }

        @Test
        @DisplayName("Should handle all status values")
        void shouldHandleAllStatusValues() {
            // Arrange
            String[] statuses = {
                    "RECEIVED",
                    "IN_DIAGNOSIS",
                    "WAITING_APPROVAL",
                    "IN_EXECUTION",
                    "FINISHED",
                    "DELIVERED",
                    "CANCELLED"
            };

            // Act & Assert
            for (String status : statuses) {
                ServiceOrderStatusUpdateDto dto = ServiceOrderStatusUpdateDto.builder()
                        .status(status)
                        .build();
                assertEquals(status, dto.status());
            }
        }
    }

    @Nested
    @DisplayName("ErrorMessageDto Tests")
    class ErrorMessageDtoTests {

        @Test
        @DisplayName("Should create ErrorMessageDto using builder")
        void shouldCreateErrorMessageDtoUsingBuilder() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.now();

            // Act
            ErrorMessageDto dto = ErrorMessageDto.builder()
                    .error("Bad Request")
                    .message("Validation failed")
                    .status(400)
                    .path("/api/v1/os")
                    .timestamp(timestamp)
                    .build();

            // Assert
            assertNotNull(dto);
            assertEquals("Bad Request", dto.error());
            assertEquals("Validation failed", dto.message());
            assertEquals(400, dto.status());
            assertEquals("/api/v1/os", dto.path());
            assertEquals(timestamp, dto.timestamp());
        }

        @Test
        @DisplayName("Should handle different HTTP status codes")
        void shouldHandleDifferentHttpStatusCodes() {
            // Act
            ErrorMessageDto badRequest = ErrorMessageDto.builder().status(400).build();
            ErrorMessageDto notFound = ErrorMessageDto.builder().status(404).build();
            ErrorMessageDto serverError = ErrorMessageDto.builder().status(500).build();

            // Assert
            assertEquals(400, badRequest.status());
            assertEquals(404, notFound.status());
            assertEquals(500, serverError.status());
        }
    }

    @Nested
    @DisplayName("PageDto Tests")
    class PageDtoTests {

        @Test
        @DisplayName("Should create PageDto with content")
        void shouldCreatePageDtoWithContent() {
            // Arrange
            List<ServiceOrderDto> content = List.of(
                    ServiceOrderDto.builder().id(1L).customerId(1L).vehicleId(1L).services(List.of())
                            .resources(List.of()).build(),
                    ServiceOrderDto.builder().id(2L).customerId(2L).vehicleId(2L).services(List.of())
                            .resources(List.of()).build());

            // Act
            PageDto<ServiceOrderDto> page = new PageDto<>(content, 100, 0, 10);

            // Assert
            assertNotNull(page);
            assertEquals(2, page.content().size());
            assertEquals(100, page.totalElements());
            assertEquals(0, page.pageNumber());
            assertEquals(10, page.pageSize());
        }

        @Test
        @DisplayName("Should handle empty page")
        void shouldHandleEmptyPage() {
            // Act
            PageDto<ServiceOrderDto> page = new PageDto<>(List.of(), 0, 0, 10);

            // Assert
            assertTrue(page.content().isEmpty());
            assertEquals(0, page.totalElements());
        }

        @Test
        @DisplayName("Should handle different generic types")
        void shouldHandleDifferentGenericTypes() {
            // Act
            PageDto<String> stringPage = new PageDto<>(List.of("a", "b", "c"), 3, 0, 10);
            PageDto<Long> longPage = new PageDto<>(List.of(1L, 2L), 2, 0, 10);

            // Assert
            assertEquals(3, stringPage.content().size());
            assertEquals(2, longPage.content().size());
        }
    }

    @Nested
    @DisplayName("PageRequestDto Tests")
    class PageRequestDtoTests {

        @Test
        @DisplayName("Should create PageRequestDto")
        void shouldCreatePageRequestDto() {
            // Act
            PageRequestDto dto = new PageRequestDto(0, 20);

            // Assert
            assertEquals(0, dto.page());
            assertEquals(20, dto.size());
        }

        @Test
        @DisplayName("Should handle different page numbers")
        void shouldHandleDifferentPageNumbers() {
            // Act
            PageRequestDto firstPage = new PageRequestDto(0, 10);
            PageRequestDto secondPage = new PageRequestDto(1, 10);
            PageRequestDto lastPage = new PageRequestDto(99, 10);

            // Assert
            assertEquals(0, firstPage.page());
            assertEquals(1, secondPage.page());
            assertEquals(99, lastPage.page());
        }

        @Test
        @DisplayName("Should handle different page sizes")
        void shouldHandleDifferentPageSizes() {
            // Act
            PageRequestDto smallPage = new PageRequestDto(0, 5);
            PageRequestDto mediumPage = new PageRequestDto(0, 20);
            PageRequestDto largePage = new PageRequestDto(0, 100);

            // Assert
            assertEquals(5, smallPage.size());
            assertEquals(20, mediumPage.size());
            assertEquals(100, largePage.size());
        }
    }

    @Nested
    @DisplayName("DTO Equality Tests")
    class DtoEqualityTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.of(2025, 2, 13, 12, 0);

            ServiceOrderDto dto1 = ServiceOrderDto.builder()
                    .id(1L)
                    .customerId(10L)
                    .vehicleId(20L)
                    .status("RECEIVED")
                    .createdAt(timestamp)
                    .services(List.of())
                    .resources(List.of())
                    .build();

            ServiceOrderDto dto2 = ServiceOrderDto.builder()
                    .id(1L)
                    .customerId(10L)
                    .vehicleId(20L)
                    .status("RECEIVED")
                    .createdAt(timestamp)
                    .services(List.of())
                    .resources(List.of())
                    .build();

            // Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when fields differ")
        void shouldNotBeEqualWhenFieldsDiffer() {
            // Arrange
            ServiceOrderDto dto1 = ServiceOrderDto.builder()
                    .id(1L)
                    .customerId(10L)
                    .vehicleId(20L)
                    .services(List.of())
                    .resources(List.of())
                    .build();

            ServiceOrderDto dto2 = ServiceOrderDto.builder()
                    .id(2L)
                    .customerId(10L)
                    .vehicleId(20L)
                    .services(List.of())
                    .resources(List.of())
                    .build();

            // Assert
            assertNotEquals(dto1, dto2);
        }
    }
}
