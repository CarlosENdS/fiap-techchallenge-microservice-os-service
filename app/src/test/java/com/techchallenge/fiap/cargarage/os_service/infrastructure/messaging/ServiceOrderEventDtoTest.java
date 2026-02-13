package com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ServiceOrderEventDtoTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create ServiceOrderEventDto using builder")
        void shouldCreateServiceOrderEventDtoUsingBuilder() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.now();

            // Act
            ServiceOrderEventDto dto = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(100L)
                    .customerId(1L)
                    .customerName("John Doe")
                    .vehicleId(2L)
                    .vehicleLicensePlate("ABC-1234")
                    .status("RECEIVED")
                    .description("Test service order")
                    .timestamp(timestamp)
                    .build();

            // Assert
            assertNotNull(dto);
            assertEquals("ORDER_CREATED", dto.eventType());
            assertEquals(100L, dto.orderId());
            assertEquals(1L, dto.customerId());
            assertEquals("John Doe", dto.customerName());
            assertEquals(2L, dto.vehicleId());
            assertEquals("ABC-1234", dto.vehicleLicensePlate());
            assertEquals("RECEIVED", dto.status());
            assertEquals("Test service order", dto.description());
            assertEquals(timestamp, dto.timestamp());
        }

        @Test
        @DisplayName("Should create ServiceOrderEventDto with null values")
        void shouldCreateServiceOrderEventDtoWithNullValues() {
            // Act
            ServiceOrderEventDto dto = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CANCELLED")
                    .orderId(200L)
                    .customerId(null)
                    .customerName(null)
                    .vehicleId(null)
                    .vehicleLicensePlate(null)
                    .status(null)
                    .description(null)
                    .timestamp(null)
                    .build();

            // Assert
            assertNotNull(dto);
            assertEquals("ORDER_CANCELLED", dto.eventType());
            assertEquals(200L, dto.orderId());
            assertNull(dto.customerId());
            assertNull(dto.customerName());
            assertNull(dto.vehicleId());
            assertNull(dto.vehicleLicensePlate());
            assertNull(dto.status());
            assertNull(dto.description());
            assertNull(dto.timestamp());
        }

        @Test
        @DisplayName("Should build with all event types")
        void shouldBuildWithAllEventTypes() {
            // Act & Assert
            String[] eventTypes = {
                    "ORDER_CREATED",
                    "ORDER_WAITING_APPROVAL",
                    "ORDER_APPROVED",
                    "ORDER_REJECTED",
                    "ORDER_FINISHED",
                    "ORDER_DELIVERED",
                    "ORDER_CANCELLED"
            };

            for (String eventType : eventTypes) {
                ServiceOrderEventDto dto = ServiceOrderEventDto.builder()
                        .eventType(eventType)
                        .orderId(1L)
                        .customerId(1L)
                        .vehicleId(1L)
                        .timestamp(LocalDateTime.now())
                        .build();

                assertEquals(eventType, dto.eventType());
            }
        }
    }

    @Nested
    @DisplayName("Record Accessor Tests")
    class RecordAccessorTests {

        @Test
        @DisplayName("Should access all fields via record accessors")
        void shouldAccessAllFieldsViaRecordAccessors() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.of(2025, 2, 13, 10, 30);

            ServiceOrderEventDto dto = ServiceOrderEventDto.builder()
                    .eventType("ORDER_APPROVED")
                    .orderId(500L)
                    .customerId(50L)
                    .customerName("Jane Smith")
                    .vehicleId(100L)
                    .vehicleLicensePlate("XYZ-9876")
                    .status("IN_EXECUTION")
                    .description("Vehicle maintenance")
                    .timestamp(timestamp)
                    .build();

            // Act & Assert
            assertEquals("ORDER_APPROVED", dto.eventType());
            assertEquals(500L, dto.orderId());
            assertEquals(50L, dto.customerId());
            assertEquals("Jane Smith", dto.customerName());
            assertEquals(100L, dto.vehicleId());
            assertEquals("XYZ-9876", dto.vehicleLicensePlate());
            assertEquals("IN_EXECUTION", dto.status());
            assertEquals("Vehicle maintenance", dto.description());
            assertEquals(timestamp, dto.timestamp());
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.of(2025, 2, 13, 12, 0);

            ServiceOrderEventDto dto1 = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(1L)
                    .customerId(10L)
                    .customerName("Test Customer")
                    .vehicleId(20L)
                    .vehicleLicensePlate("TEST-123")
                    .status("RECEIVED")
                    .description("Test")
                    .timestamp(timestamp)
                    .build();

            ServiceOrderEventDto dto2 = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(1L)
                    .customerId(10L)
                    .customerName("Test Customer")
                    .vehicleId(20L)
                    .vehicleLicensePlate("TEST-123")
                    .status("RECEIVED")
                    .description("Test")
                    .timestamp(timestamp)
                    .build();

            // Act & Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when fields differ")
        void shouldNotBeEqualWhenFieldsDiffer() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.now();

            ServiceOrderEventDto dto1 = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(1L)
                    .customerId(10L)
                    .timestamp(timestamp)
                    .build();

            ServiceOrderEventDto dto2 = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CANCELLED")
                    .orderId(1L)
                    .customerId(10L)
                    .timestamp(timestamp)
                    .build();

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal when orderId differs")
        void shouldNotBeEqualWhenOrderIdDiffers() {
            // Arrange
            ServiceOrderEventDto dto1 = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(1L)
                    .build();

            ServiceOrderEventDto dto2 = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(2L)
                    .build();

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate toString representation")
        void shouldGenerateToStringRepresentation() {
            // Arrange
            ServiceOrderEventDto dto = ServiceOrderEventDto.builder()
                    .eventType("ORDER_FINISHED")
                    .orderId(999L)
                    .customerId(88L)
                    .customerName("Test User")
                    .build();

            // Act
            String toString = dto.toString();

            // Assert
            assertNotNull(toString);
            assertTrue(toString.contains("ORDER_FINISHED"));
            assertTrue(toString.contains("999"));
            assertTrue(toString.contains("88"));
            assertTrue(toString.contains("Test User"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty string values")
        void shouldHandleEmptyStringValues() {
            // Act
            ServiceOrderEventDto dto = ServiceOrderEventDto.builder()
                    .eventType("")
                    .orderId(1L)
                    .customerName("")
                    .vehicleLicensePlate("")
                    .status("")
                    .description("")
                    .build();

            // Assert
            assertEquals("", dto.eventType());
            assertEquals("", dto.customerName());
            assertEquals("", dto.vehicleLicensePlate());
            assertEquals("", dto.status());
            assertEquals("", dto.description());
        }

        @Test
        @DisplayName("Should handle special characters in strings")
        void shouldHandleSpecialCharactersInStrings() {
            // Act
            ServiceOrderEventDto dto = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(1L)
                    .customerName("José García-López")
                    .vehicleLicensePlate("ÄBC-123")
                    .description("Test with special chars: @#$%^&*()")
                    .build();

            // Assert
            assertEquals("José García-López", dto.customerName());
            assertEquals("ÄBC-123", dto.vehicleLicensePlate());
            assertTrue(dto.description().contains("@#$%^&*()"));
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Arrange
            String longDescription = "A".repeat(10000);

            // Act
            ServiceOrderEventDto dto = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(1L)
                    .description(longDescription)
                    .build();

            // Assert
            assertEquals(10000, dto.description().length());
        }

        @Test
        @DisplayName("Should handle zero and negative IDs")
        void shouldHandleZeroAndNegativeIds() {
            // Act
            ServiceOrderEventDto dtoWithZero = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(0L)
                    .customerId(0L)
                    .vehicleId(0L)
                    .build();

            ServiceOrderEventDto dtoWithNegative = ServiceOrderEventDto.builder()
                    .eventType("ORDER_CREATED")
                    .orderId(-1L)
                    .customerId(-100L)
                    .vehicleId(-50L)
                    .build();

            // Assert
            assertEquals(0L, dtoWithZero.orderId());
            assertEquals(-1L, dtoWithNegative.orderId());
            assertEquals(-100L, dtoWithNegative.customerId());
        }
    }
}
