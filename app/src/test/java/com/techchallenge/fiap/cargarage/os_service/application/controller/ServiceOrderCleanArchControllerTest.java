package com.techchallenge.fiap.cargarage.os_service.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderApprovalDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderExecutionTimeDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusUpdateDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderItem;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderResource;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CancelServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CreateServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.FindServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.GetServiceOrderExecutionTimeUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.ProcessApprovalUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderStatusUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderUseCase;

@ExtendWith(MockitoExtension.class)
class ServiceOrderCleanArchControllerTest {

    @Mock
    private FindServiceOrderUseCase findServiceOrderUseCase;
    @Mock
    private CreateServiceOrderUseCase createServiceOrderUseCase;
    @Mock
    private UpdateServiceOrderUseCase updateServiceOrderUseCase;
    @Mock
    private UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase;
    @Mock
    private ProcessApprovalUseCase processApprovalUseCase;
    @Mock
    private GetServiceOrderExecutionTimeUseCase getServiceOrderExecutionTimeUseCase;
    @Mock
    private CancelServiceOrderUseCase cancelServiceOrderUseCase;

    private ServiceOrderCleanArchController controller;

    @BeforeEach
    void setUp() {
        controller = new ServiceOrderCleanArchController(
                findServiceOrderUseCase,
                createServiceOrderUseCase,
                updateServiceOrderUseCase,
                updateServiceOrderStatusUseCase,
                processApprovalUseCase,
                getServiceOrderExecutionTimeUseCase,
                cancelServiceOrderUseCase);
    }

    @Test
    @DisplayName("Should find service order by id")
    void shouldFindById() {
        ServiceOrder order = createOrder(1L, ServiceOrderStatus.received());
        when(findServiceOrderUseCase.findById(1L)).thenReturn(order);

        ServiceOrderDto result = controller.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("RECEIVED", result.status());
        verify(findServiceOrderUseCase).findById(1L);
    }

    @Test
    @DisplayName("Should find all with pagination")
    void shouldFindAll() {
        ServiceOrder order = createOrder(10L, ServiceOrderStatus.inDiagnosis());
        when(findServiceOrderUseCase.findAll(any())).thenReturn(new PageDto<>(List.of(order), 1, 2, 5));

        PageDto<ServiceOrderDto> result = controller.findAll(2, 5);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(2, result.pageNumber());
        assertEquals(5, result.pageSize());
        assertEquals("IN_DIAGNOSIS", result.content().get(0).status());

        ArgumentCaptor<com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto> captor = ArgumentCaptor
                .forClass(com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto.class);
        verify(findServiceOrderUseCase).findAll(captor.capture());
        assertEquals(2, captor.getValue().page());
        assertEquals(5, captor.getValue().size());
    }

    @Test
    @DisplayName("Should find by customer id with pagination")
    void shouldFindByCustomerId() {
        ServiceOrder order = createOrder(11L, ServiceOrderStatus.waitingApproval());
        when(findServiceOrderUseCase.findByCustomerId(eq(99L), any()))
                .thenReturn(new PageDto<>(List.of(order), 1, 0, 10));

        PageDto<ServiceOrderDto> result = controller.findByCustomerId(99L, 0, 10);

        assertEquals(1, result.content().size());
        assertEquals(99L, result.content().get(0).customerId());
        verify(findServiceOrderUseCase).findByCustomerId(eq(99L), any());
    }

    @Test
    @DisplayName("Should find by status with pagination")
    void shouldFindByStatus() {
        ServiceOrder order = createOrder(12L, ServiceOrderStatus.finished());
        when(findServiceOrderUseCase.findByStatus(eq("FINISHED"), any()))
                .thenReturn(new PageDto<>(List.of(order), 1, 0, 10));

        PageDto<ServiceOrderDto> result = controller.findByStatus("FINISHED", 0, 10);

        assertEquals(1, result.content().size());
        assertEquals("FINISHED", result.content().get(0).status());
        verify(findServiceOrderUseCase).findByStatus(eq("FINISHED"), any());
    }

    @Test
    @DisplayName("Should return execution time statistics")
    void shouldGetExecutionTimeStatistics() {
        ServiceOrderExecutionTimeDto stats = ServiceOrderExecutionTimeDto.builder()
                .totalOrders(8)
                .avgExecutionTimeHours(new BigDecimal("4.50"))
                .minExecutionTimeHours(new BigDecimal("2.00"))
                .maxExecutionTimeHours(new BigDecimal("9.00"))
                .ordersInProgress(2)
                .ordersFinished(6)
                .ordersDelivered(5)
                .build();
        when(getServiceOrderExecutionTimeUseCase.execute()).thenReturn(stats);

        ServiceOrderExecutionTimeDto result = controller.getExecutionTimeStatistics();

        assertEquals(8L, result.totalOrders());
        assertEquals(new BigDecimal("4.50"), result.avgExecutionTimeHours());
        verify(getServiceOrderExecutionTimeUseCase).execute();
    }

    @Test
    @DisplayName("Should create service order")
    void shouldCreate() {
        ServiceOrderRequestDto request = ServiceOrderRequestDto.builder()
                .customerId(99L)
                .customerName("Customer")
                .vehicleId(88L)
                .vehicleLicensePlate("AAA-1234")
                .vehicleModel("Model")
                .vehicleBrand("Brand")
                .description("Create")
                .services(List.of())
                .resources(List.of())
                .build();
        when(createServiceOrderUseCase.execute(request))
                .thenReturn(createOrder(20L, ServiceOrderStatus.received()));

        ServiceOrderDto result = controller.create(request);

        assertEquals(20L, result.id());
        assertEquals("RECEIVED", result.status());
        verify(createServiceOrderUseCase).execute(request);
    }

    @Test
    @DisplayName("Should update service order")
    void shouldUpdate() {
        ServiceOrderRequestDto request = ServiceOrderRequestDto.builder()
                .customerId(99L)
                .customerName("Customer")
                .vehicleId(88L)
                .vehicleLicensePlate("AAA-1234")
                .vehicleModel("Model")
                .vehicleBrand("Brand")
                .description("Update")
                .services(List.of())
                .resources(List.of())
                .build();
        when(updateServiceOrderUseCase.execute(21L, request))
                .thenReturn(createOrder(21L, ServiceOrderStatus.inDiagnosis()));

        ServiceOrderDto result = controller.update(21L, request);

        assertEquals(21L, result.id());
        assertEquals("IN_DIAGNOSIS", result.status());
        verify(updateServiceOrderUseCase).execute(21L, request);
    }

    @Test
    @DisplayName("Should update service order status")
    void shouldUpdateStatus() {
        ServiceOrderStatusUpdateDto statusDto = ServiceOrderStatusUpdateDto.builder()
                .status("IN_EXECUTION")
                .build();
        when(updateServiceOrderStatusUseCase.execute(22L, statusDto))
                .thenReturn(createOrder(22L, ServiceOrderStatus.inExecution()));

        ServiceOrderDto result = controller.updateStatus(22L, statusDto);

        assertEquals(22L, result.id());
        assertEquals("IN_EXECUTION", result.status());
        verify(updateServiceOrderStatusUseCase).execute(22L, statusDto);
    }

    @Test
    @DisplayName("Should process approval")
    void shouldProcessApproval() {
        ServiceOrderApprovalDto approvalDto = new ServiceOrderApprovalDto(true);
        when(processApprovalUseCase.execute(23L, true))
                .thenReturn(createOrder(23L, ServiceOrderStatus.inExecution()));

        ServiceOrderDto result = controller.processApproval(23L, approvalDto);

        assertEquals(23L, result.id());
        assertEquals("IN_EXECUTION", result.status());
        verify(processApprovalUseCase).execute(23L, true);
    }

    @Test
    @DisplayName("Should get service order status dto")
    void shouldGetStatus() {
        when(findServiceOrderUseCase.findById(24L))
                .thenReturn(createOrder(24L, ServiceOrderStatus.delivered()));

        ServiceOrderStatusDto result = controller.getStatus(24L);

        assertNotNull(result);
        assertEquals("DELIVERED", result.status());
        verify(findServiceOrderUseCase).findById(24L);
    }

    @Test
    @DisplayName("Should return null status when model status is null")
    void shouldGetNullStatusWhenOrderStatusIsNull() {
        ServiceOrder orderWithNullStatus = ServiceOrder.builder()
                .id(25L)
                .customerId(99L)
                .customerName("Customer")
                .vehicleId(88L)
                .vehicleLicensePlate("AAA-1234")
                .vehicleModel("Model")
                .vehicleBrand("Brand")
                .description("No status")
                .status(null)
                .totalPrice(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .services(List.of())
                .resources(List.of())
                .build();
        when(findServiceOrderUseCase.findById(25L)).thenReturn(orderWithNullStatus);

        ServiceOrderStatusDto result = controller.getStatus(25L);

        assertNotNull(result);
        assertNull(result.status());
    }

    @Test
    @DisplayName("Should cancel service order")
    void shouldCancel() {
        when(cancelServiceOrderUseCase.execute(26L, "Out of stock"))
                .thenReturn(createOrder(26L, ServiceOrderStatus.cancelled()));

        ServiceOrderDto result = controller.cancel(26L, "Out of stock");

        assertEquals(26L, result.id());
        assertEquals("CANCELLED", result.status());
        verify(cancelServiceOrderUseCase).execute(26L, "Out of stock");
    }

    private ServiceOrder createOrder(Long id, ServiceOrderStatus status) {
        ServiceOrderItem item = ServiceOrderItem.builder()
                .id(1L)
                .serviceId(10L)
                .serviceName("Diagnostics")
                .serviceDescription("Engine diagnostics")
                .quantity(1)
                .price(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("100.00"))
                .build();

        ServiceOrderResource resource = ServiceOrderResource.builder()
                .id(2L)
                .resourceId(20L)
                .resourceName("Oil")
                .resourceDescription("Synthetic oil")
                .resourceType("PART")
                .quantity(1)
                .price(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("50.00"))
                .build();

        return ServiceOrder.builder()
                .id(id)
                .customerId(99L)
                .customerName("Customer")
                .vehicleId(88L)
                .vehicleLicensePlate("AAA-1234")
                .vehicleModel("Model")
                .vehicleBrand("Brand")
                .description("Issue")
                .status(status)
                .totalPrice(new BigDecimal("150.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .services(List.of(item))
                .resources(List.of(resource))
                .build();
    }
}
