package com.techchallenge.fiap.cargarage.os_service.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import com.techchallenge.fiap.cargarage.os_service.application.dto.ErrorMessageDto;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;
import com.techchallenge.fiap.cargarage.os_service.application.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("Handle MethodArgumentNotValid Tests")
    class HandleMethodArgumentNotValidTests {

        @Test
        @DisplayName("Should return BAD_REQUEST for validation errors")
        void shouldReturnBadRequestForValidationErrors() {
            // Arrange
            FieldError fieldError1 = new FieldError("request", "status", "status is required");
            FieldError fieldError2 = new FieldError("request", "customerId", "must not be null");
            List<FieldError> fieldErrors = List.of(fieldError1, fieldError2);

            when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os");

            HttpHeaders headers = new HttpHeaders();
            HttpStatusCode status = HttpStatus.BAD_REQUEST;

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                    methodArgumentNotValidException, headers, status, webRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            ErrorMessageDto errorMessage = (ErrorMessageDto) response.getBody();
            assertNotNull(errorMessage);
            assertEquals("Validation Error", errorMessage.error());
            assertTrue(errorMessage.message().contains("status"));
            assertTrue(errorMessage.message().contains("customerId"));
            assertEquals(400, errorMessage.status());
            assertEquals("/api/v1/os", errorMessage.path());
            assertNotNull(errorMessage.timestamp());
        }

        @Test
        @DisplayName("Should handle single field error")
        void shouldHandleSingleFieldError() {
            // Arrange
            FieldError fieldError = new FieldError("request", "description", "must not be blank");
            List<FieldError> fieldErrors = List.of(fieldError);

            when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os/100");

            HttpHeaders headers = new HttpHeaders();
            HttpStatusCode status = HttpStatus.BAD_REQUEST;

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                    methodArgumentNotValidException, headers, status, webRequest);

            // Assert
            assertNotNull(response);
            ErrorMessageDto errorMessage = (ErrorMessageDto) response.getBody();
            assertNotNull(errorMessage);
            assertTrue(errorMessage.message().contains("description"));
        }
    }

    @Nested
    @DisplayName("Handle InvalidDataException Tests")
    class HandleInvalidDataExceptionTests {

        @Test
        @DisplayName("Should return BAD_REQUEST for InvalidDataException")
        void shouldReturnBadRequestForInvalidDataException() {
            // Arrange
            InvalidDataException exception = new InvalidDataException("Invalid order status");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os/100/status");

            // Act
            ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleInvalidDataException(
                    exception, webRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            ErrorMessageDto errorMessage = response.getBody();
            assertNotNull(errorMessage);
            assertEquals(HttpStatus.BAD_REQUEST.toString(), errorMessage.error());
            assertEquals("Invalid order status", errorMessage.message());
            assertEquals(400, errorMessage.status());
            assertEquals("/api/v1/os/100/status", errorMessage.path());
        }

        @Test
        @DisplayName("Should include timestamp in response")
        void shouldIncludeTimestampInResponse() {
            // Arrange
            InvalidDataException exception = new InvalidDataException("Test exception");
            when(webRequest.getDescription(false)).thenReturn("uri=/test");

            // Act
            ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleInvalidDataException(
                    exception, webRequest);

            // Assert
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().timestamp());
        }
    }

    @Nested
    @DisplayName("Handle NotFoundException Tests")
    class HandleNotFoundExceptionTests {

        @Test
        @DisplayName("Should return NOT_FOUND for NotFoundException")
        void shouldReturnNotFoundForNotFoundException() {
            // Arrange
            NotFoundException exception = new NotFoundException("Order not found with id: 999");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os/999");

            // Act
            ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleNotFoundException(
                    exception, webRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

            ErrorMessageDto errorMessage = response.getBody();
            assertNotNull(errorMessage);
            assertEquals(HttpStatus.NOT_FOUND.toString(), errorMessage.error());
            assertEquals("Order not found with id: 999", errorMessage.message());
            assertEquals(404, errorMessage.status());
            assertEquals("/api/v1/os/999", errorMessage.path());
        }

        @Test
        @DisplayName("Should handle multiple resource not found")
        void shouldHandleMultipleResourceNotFound() {
            // Arrange
            NotFoundException exception = new NotFoundException("Customer with id 123 not found");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/customers/123");

            // Act
            ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleNotFoundException(
                    exception, webRequest);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("Customer with id 123 not found", response.getBody().message());
        }
    }

    @Nested
    @DisplayName("Handle IllegalArgumentException Tests")
    class HandleIllegalArgumentExceptionTests {

        @Test
        @DisplayName("Should return BAD_REQUEST for IllegalArgumentException")
        void shouldReturnBadRequestForIllegalArgumentException() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os");

            // Act
            ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleIllegalArgumentException(
                    exception, webRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            ErrorMessageDto errorMessage = response.getBody();
            assertNotNull(errorMessage);
            assertEquals(HttpStatus.BAD_REQUEST.toString(), errorMessage.error());
            assertEquals("Invalid argument provided", errorMessage.message());
            assertEquals(400, errorMessage.status());
        }

        @Test
        @DisplayName("Should handle null pointer as illegal argument")
        void shouldHandleNullPointerAsIllegalArgument() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Parameter cannot be null");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os");

            // Act
            ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleIllegalArgumentException(
                    exception, webRequest);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Parameter cannot be null", response.getBody().message());
        }
    }

    @Nested
    @DisplayName("Handle All Exceptions Tests")
    class HandleAllExceptionsTests {

        @Test
        @DisplayName("Should return INTERNAL_SERVER_ERROR for generic Exception")
        void shouldReturnInternalServerErrorForGenericException() {
            // Arrange
            Exception exception = new Exception("Unexpected error occurred");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception, webRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

            ErrorMessageDto errorMessage = (ErrorMessageDto) response.getBody();
            assertNotNull(errorMessage);
            assertEquals("Internal Server Error", errorMessage.error());
            assertEquals("Unexpected error occurred", errorMessage.message());
            assertEquals(500, errorMessage.status());
        }

        @Test
        @DisplayName("Should handle RuntimeException as generic exception")
        void shouldHandleRuntimeExceptionAsGenericException() {
            // Arrange
            RuntimeException exception = new RuntimeException("Runtime error");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os/100");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception, webRequest);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            ErrorMessageDto errorMessage = (ErrorMessageDto) response.getBody();
            assertEquals("Runtime error", errorMessage.message());
        }

        @Test
        @DisplayName("Should handle NullPointerException")
        void shouldHandleNullPointerException() {
            // Arrange
            NullPointerException exception = new NullPointerException("Value is null");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception, webRequest);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }

        @Test
        @DisplayName("Should include correct path from request")
        void shouldIncludeCorrectPathFromRequest() {
            // Arrange
            Exception exception = new Exception("Test");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/os/123/items");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception, webRequest);

            // Assert
            ErrorMessageDto errorMessage = (ErrorMessageDto) response.getBody();
            assertEquals("/api/v1/os/123/items", errorMessage.path());
        }
    }

    @Nested
    @DisplayName("Path Parsing Tests")
    class PathParsingTests {

        @Test
        @DisplayName("Should correctly strip uri= prefix from path")
        void shouldCorrectlyStripUriPrefixFromPath() {
            // Arrange
            NotFoundException exception = new NotFoundException("Not found");
            when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/resources/456");

            // Act
            ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleNotFoundException(
                    exception, webRequest);

            // Assert
            assertEquals("/api/v1/resources/456", response.getBody().path());
        }

        @Test
        @DisplayName("Should handle empty uri prefix")
        void shouldHandleEmptyUriPrefix() {
            // Arrange
            NotFoundException exception = new NotFoundException("Not found");
            when(webRequest.getDescription(false)).thenReturn("/api/v1/test");

            // Act
            ResponseEntity<ErrorMessageDto> response = exceptionHandler.handleNotFoundException(
                    exception, webRequest);

            // Assert
            assertEquals("/api/v1/test", response.getBody().path());
        }
    }
}
