package com.techchallenge.fiap.cargarage.os_service.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.techchallenge.fiap.cargarage.os_service.application.controller.ServiceOrderCleanArchController;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ErrorMessageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderApprovalDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderExecutionTimeDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusUpdateDto;

/**
 * REST controller for Service Order endpoints.
 */
@Tag(name = "Service Orders", description = "Service Order (OS) management endpoints")
@RestController
@RequiredArgsConstructor
@RequestMapping("/service-orders")
public class ServiceOrderController {

    private final ServiceOrderCleanArchController serviceOrderController;

    @Operation(summary = "Get service order by ID", description = "Returns a service order by its ID")
    @ApiResponse(responseCode = "200", description = "Service order found")
    @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderDto> findById(
            @Parameter(description = "Service order ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(serviceOrderController.findById(id));
    }

    @Operation(summary = "Get all service orders", description = "Returns a paginated list of service orders")
    @ApiResponse(responseCode = "200", description = "List of service orders")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping
    public ResponseEntity<PageDto<ServiceOrderDto>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {
        PageDto<ServiceOrderDto> pageDto = serviceOrderController.findAll(page, size);
        return ResponseEntity.ok(pageDto);
    }

    @Operation(summary = "Get service orders by customer", description = "Returns a paginated list of service orders for a specific customer")
    @ApiResponse(responseCode = "200", description = "List of service orders")
    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<PageDto<ServiceOrderDto>> findByCustomerId(
            @Parameter(description = "Customer ID", required = true) @PathVariable Long customerId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {
        return ResponseEntity.ok(
                serviceOrderController.findByCustomerId(customerId, page, size));
    }

    @Operation(summary = "Get service orders by status", description = "Returns a paginated list of service orders with a specific status")
    @ApiResponse(responseCode = "200", description = "List of service orders")
    @ApiResponse(responseCode = "400", description = "Invalid status", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/status/{status}")
    public ResponseEntity<PageDto<ServiceOrderDto>> findByStatus(
            @Parameter(description = "Service order status", required = true) @PathVariable String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {
        return ResponseEntity.ok(
                serviceOrderController.findByStatus(status, page, size));
    }

    @Operation(summary = "Get execution time statistics", description = "Returns statistics about service order execution times")
    @ApiResponse(responseCode = "200", description = "Execution time statistics")
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/stats/execution-time")
    public ResponseEntity<ServiceOrderExecutionTimeDto> getExecutionTimeStatistics() {
        return ResponseEntity.ok(serviceOrderController.getExecutionTimeStatistics());
    }

    @Operation(summary = "Create a new service order", description = "Creates a new service order")
    @ApiResponse(responseCode = "201", description = "Service order created")
    @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @PostMapping
    public ResponseEntity<ServiceOrderDto> create(
            @Valid @RequestBody ServiceOrderRequestDto orderDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(serviceOrderController.create(orderDto));
    }

    @Operation(summary = "Update a service order", description = "Updates an existing service order")
    @ApiResponse(responseCode = "200", description = "Service order updated")
    @ApiResponse(responseCode = "400", description = "Invalid input data or order status", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @PutMapping("/{id}")
    public ResponseEntity<ServiceOrderDto> update(
            @Parameter(description = "Service order ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ServiceOrderRequestDto orderDto) {
        return ResponseEntity.ok(serviceOrderController.update(id, orderDto));
    }

    @Operation(summary = "Update service order status", description = "Updates the status of an existing service order")
    @ApiResponse(responseCode = "200", description = "Service order status updated")
    @ApiResponse(responseCode = "400", description = "Invalid status transition", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceOrderDto> updateStatus(
            @Parameter(description = "Service order ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ServiceOrderStatusUpdateDto statusDto) {
        return ResponseEntity.ok(serviceOrderController.updateStatus(id, statusDto));
    }

    @Operation(summary = "Process service order approval", description = "Processes customer approval for a service order")
    @ApiResponse(responseCode = "200", description = "Service order approval processed")
    @ApiResponse(responseCode = "400", description = "Service order is not waiting for approval", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @PutMapping("/{id}/approve")
    public ResponseEntity<ServiceOrderDto> processApproval(
            @Parameter(description = "Service order ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ServiceOrderApprovalDto approvalDto) {
        return ResponseEntity.ok(serviceOrderController.processApproval(id, approvalDto));
    }

    @Operation(summary = "Get service order status", description = "Returns the current status of a service order")
    @ApiResponse(responseCode = "200", description = "Service order status returned")
    @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/{id}/status")
    public ResponseEntity<ServiceOrderStatusDto> getStatus(
            @Parameter(description = "Service order ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(serviceOrderController.getStatus(id));
    }

    @Operation(summary = "Cancel a service order", description = "Cancels an existing service order")
    @ApiResponse(responseCode = "200", description = "Service order cancelled")
    @ApiResponse(responseCode = "400", description = "Cannot cancel order in current status", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceOrderDto> cancel(
            @Parameter(description = "Service order ID", required = true) @PathVariable Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        return ResponseEntity.ok(serviceOrderController.cancel(id, reason));
    }
}
