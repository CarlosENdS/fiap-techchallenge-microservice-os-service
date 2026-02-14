package com.techchallenge.fiap.cargarage.os_service.application.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExceptionTest {

    @Nested
    @DisplayName("BusinessException Tests")
    class BusinessExceptionTests {

        @Test
        @DisplayName("Should create BusinessException with message")
        void shouldCreateBusinessExceptionWithMessage() {
            // Arrange
            String errorMessage = "Business rule violation";

            // Act
            BusinessException exception = new BusinessException(errorMessage);

            // Assert
            assertNotNull(exception);
            assertEquals(errorMessage, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create BusinessException with message and cause")
        void shouldCreateBusinessExceptionWithMessageAndCause() {
            // Arrange
            String errorMessage = "Business operation failed";
            Throwable cause = new RuntimeException("Root cause");

            // Act
            BusinessException exception = new BusinessException(errorMessage, cause);

            // Assert
            assertNotNull(exception);
            assertEquals(errorMessage, exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertEquals("Root cause", exception.getCause().getMessage());
        }

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            // Arrange
            BusinessException exception = new BusinessException("Test exception");

            // Act & Assert
            assertThrows(BusinessException.class, () -> {
                throw exception;
            });
        }

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            // Arrange
            BusinessException exception = new BusinessException("Test");

            // Assert
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should preserve exception chain")
        void shouldPreserveExceptionChain() {
            // Arrange
            IllegalStateException rootCause = new IllegalStateException("Root");
            RuntimeException intermediateCause = new RuntimeException("Intermediate", rootCause);
            BusinessException exception = new BusinessException("Top level", intermediateCause);

            // Assert
            assertEquals("Top level", exception.getMessage());
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
        }
    }

    @Nested
    @DisplayName("NotFoundException Tests")
    class NotFoundExceptionTests {

        @Test
        @DisplayName("Should create NotFoundException with message")
        void shouldCreateNotFoundExceptionWithMessage() {
            // Arrange
            String errorMessage = "Order not found with id: 999";

            // Act
            NotFoundException exception = new NotFoundException(errorMessage);

            // Assert
            assertNotNull(exception);
            assertEquals(errorMessage, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create NotFoundException with message and cause")
        void shouldCreateNotFoundExceptionWithMessageAndCause() {
            // Arrange
            String errorMessage = "Resource not found";
            Throwable cause = new Exception("Database error");

            // Act
            NotFoundException exception = new NotFoundException(errorMessage, cause);

            // Assert
            assertNotNull(exception);
            assertEquals(errorMessage, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should be instance of BusinessException")
        void shouldBeInstanceOfBusinessException() {
            // Arrange
            NotFoundException exception = new NotFoundException("Not found");

            // Assert
            assertTrue(exception instanceof BusinessException);
        }

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            // Arrange
            NotFoundException exception = new NotFoundException("Not found");

            // Assert
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should be catchable as BusinessException")
        void shouldBeCatchableAsBusinessException() {
            // Arrange
            NotFoundException notFoundException = new NotFoundException("Test");

            // Act & Assert
            try {
                throw notFoundException;
            } catch (BusinessException e) {
                assertEquals("Test", e.getMessage());
            }
        }

        @Test
        @DisplayName("Should handle various resource types in message")
        void shouldHandleVariousResourceTypesInMessage() {
            // Act
            NotFoundException orderException = new NotFoundException("Order not found with id: 123");
            NotFoundException customerException = new NotFoundException("Customer not found with id: 456");
            NotFoundException vehicleException = new NotFoundException("Vehicle not found with id: 789");

            // Assert
            assertTrue(orderException.getMessage().contains("Order"));
            assertTrue(customerException.getMessage().contains("Customer"));
            assertTrue(vehicleException.getMessage().contains("Vehicle"));
        }
    }

    @Nested
    @DisplayName("InvalidDataException Tests")
    class InvalidDataExceptionTests {

        @Test
        @DisplayName("Should create InvalidDataException with message")
        void shouldCreateInvalidDataExceptionWithMessage() {
            // Arrange
            String errorMessage = "Invalid status provided";

            // Act
            InvalidDataException exception = new InvalidDataException(errorMessage);

            // Assert
            assertNotNull(exception);
            assertEquals(errorMessage, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create InvalidDataException with message and cause")
        void shouldCreateInvalidDataExceptionWithMessageAndCause() {
            // Arrange
            String errorMessage = "Invalid input data";
            Throwable cause = new IllegalArgumentException("Parse error");

            // Act
            InvalidDataException exception = new InvalidDataException(errorMessage, cause);

            // Assert
            assertNotNull(exception);
            assertEquals(errorMessage, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should be instance of BusinessException")
        void shouldBeInstanceOfBusinessException() {
            // Arrange
            InvalidDataException exception = new InvalidDataException("Invalid data");

            // Assert
            assertTrue(exception instanceof BusinessException);
        }

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            // Arrange
            InvalidDataException exception = new InvalidDataException("Invalid data");

            // Assert
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should be catchable as BusinessException")
        void shouldBeCatchableAsBusinessException() {
            // Arrange
            InvalidDataException invalidDataException = new InvalidDataException("Invalid");

            // Act & Assert
            try {
                throw invalidDataException;
            } catch (BusinessException e) {
                assertEquals("Invalid", e.getMessage());
            }
        }

        @Test
        @DisplayName("Should handle validation error messages")
        void shouldHandleValidationErrorMessages() {
            // Act
            InvalidDataException statusException = new InvalidDataException("Invalid order status: UNKNOWN");
            InvalidDataException nullException = new InvalidDataException("customerId must not be null");
            InvalidDataException rangeException = new InvalidDataException("quantity must be greater than zero");

            // Assert
            assertTrue(statusException.getMessage().contains("status"));
            assertTrue(nullException.getMessage().contains("null"));
            assertTrue(rangeException.getMessage().contains("greater than"));
        }
    }

    @Nested
    @DisplayName("Exception Hierarchy Tests")
    class ExceptionHierarchyTests {

        @Test
        @DisplayName("Should have correct inheritance hierarchy for NotFoundException")
        void shouldHaveCorrectHierarchyForNotFoundException() {
            // Arrange
            NotFoundException exception = new NotFoundException("Test");

            // Assert
            assertTrue(exception instanceof NotFoundException);
            assertTrue(exception instanceof BusinessException);
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof Exception);
            assertTrue(exception instanceof Throwable);
        }

        @Test
        @DisplayName("Should have correct inheritance hierarchy for InvalidDataException")
        void shouldHaveCorrectHierarchyForInvalidDataException() {
            // Arrange
            InvalidDataException exception = new InvalidDataException("Test");

            // Assert
            assertTrue(exception instanceof InvalidDataException);
            assertTrue(exception instanceof BusinessException);
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof Exception);
            assertTrue(exception instanceof Throwable);
        }

        @Test
        @DisplayName("Should be able to catch multiple exception types")
        void shouldBeAbleToCatchMultipleExceptionTypes() {
            // Arrange
            BusinessException[] exceptions = {
                    new BusinessException("Business error"),
                    new NotFoundException("Not found error"),
                    new InvalidDataException("Invalid data error")
            };

            // Act & Assert
            for (BusinessException exception : exceptions) {
                try {
                    throw exception;
                } catch (RuntimeException e) {
                    assertNotNull(e.getMessage());
                }
            }
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            // Act
            BusinessException businessException = new BusinessException(null);
            NotFoundException notFoundException = new NotFoundException(null);
            InvalidDataException invalidDataException = new InvalidDataException(null);

            // Assert
            assertNull(businessException.getMessage());
            assertNull(notFoundException.getMessage());
            assertNull(invalidDataException.getMessage());
        }

        @Test
        @DisplayName("Should handle empty message")
        void shouldHandleEmptyMessage() {
            // Act
            BusinessException exception = new BusinessException("");

            // Assert
            assertEquals("", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            // Act
            BusinessException exception = new BusinessException("Error", null);

            // Assert
            assertEquals("Error", exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle special characters in message")
        void shouldHandleSpecialCharactersInMessage() {
            // Arrange
            String specialMessage = "Error: <xml> \"json\" 'value' & test @ #$%^&*()";

            // Act
            BusinessException exception = new BusinessException(specialMessage);

            // Assert
            assertEquals(specialMessage, exception.getMessage());
        }

        @Test
        @DisplayName("Should handle very long message")
        void shouldHandleVeryLongMessage() {
            // Arrange
            String longMessage = "Error: " + "A".repeat(10000);

            // Act
            BusinessException exception = new BusinessException(longMessage);

            // Assert
            assertEquals(longMessage, exception.getMessage());
        }

        @Test
        @DisplayName("Should handle unicode in message")
        void shouldHandleUnicodeInMessage() {
            // Arrange
            String unicodeMessage = "错误：找不到资源 🚗 αβγδ";

            // Act
            BusinessException exception = new BusinessException(unicodeMessage);

            // Assert
            assertEquals(unicodeMessage, exception.getMessage());
        }
    }
}
