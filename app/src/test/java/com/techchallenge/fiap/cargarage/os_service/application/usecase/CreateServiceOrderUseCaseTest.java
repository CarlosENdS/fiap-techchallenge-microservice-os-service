package com.techchallenge.fiap.cargarage.os_service.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CreateServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging.ServiceOrderEventPublisher;

@ExtendWith(MockitoExtension.class)
class CreateServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderGateway gateway;

    @Mock
    private ServiceOrderEventPublisher eventPublisher;

    private CreateServiceOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateServiceOrderUseCase(gateway, eventPublisher);
    }

    @Test
    @DisplayName("Should create service order and auto-advance to WAITING_APPROVAL when quote is complete")
    void shouldCreateServiceOrderWithReceivedStatus() {
        // Arrange
        Long customerId = 1L;
        Long vehicleId = 2L;
        Long serviceId = 3L;
        Long resourceId = 4L;

        ServiceOrderItemRequestDto itemDto = ServiceOrderItemRequestDto.builder()
                .serviceId(serviceId)
                .serviceName("Oil Change")
                .serviceDescription("Complete oil change service")
                .price(new BigDecimal("150.00"))
                .quantity(1)
                .build();
        ServiceOrderResourceRequestDto resourceDto = ServiceOrderResourceRequestDto.builder()
                .resourceId(resourceId)
                .resourceName("Motor Oil 5W30")
                .resourceDescription("Synthetic motor oil")
                .resourceType("PART")
                .price(new BigDecimal("45.00"))
                .quantity(5)
                .build();

        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(customerId)
                .customerName("John Doe")
                .vehicleId(vehicleId)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Civic")
                .vehicleBrand("Honda")
                .description("Customer complaint about engine noise")
                .services(List.of(itemDto))
                .resources(List.of(resourceDto))
                .build();

        when(gateway.insert(any(ServiceOrder.class))).thenAnswer(invocation -> {
            ServiceOrder order = invocation.getArgument(0);
            return order.withId(100L);
        });
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(requestDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(customerId, result.customerId());
        assertEquals(vehicleId, result.vehicleId());
        // Auto-advance: order with complete quote (services+resources with prices)
        // transitions RECEIVED → IN_DIAGNOSIS → WAITING_APPROVAL
        assertEquals(ServiceOrderStatus.waitingApproval().value(), result.status().value());
        assertEquals(1, result.services().size());
        assertEquals(1, result.resources().size());

        // Verify events were published (ORDER_CREATED + ORDER_WAITING_APPROVAL)
        verify(eventPublisher).publishOrderCreated(any(ServiceOrder.class));
        verify(eventPublisher).publishOrderWaitingApproval(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Should calculate total amount correctly")
    void shouldCalculateTotalAmountCorrectly() {
        // Arrange
        Long customerId = 1L;
        Long vehicleId = 2L;

        ServiceOrderItemRequestDto item1 = ServiceOrderItemRequestDto.builder()
                .serviceId(10L)
                .serviceName("Service 1")
                .price(new BigDecimal("100.00"))
                .quantity(1)
                .build();
        ServiceOrderItemRequestDto item2 = ServiceOrderItemRequestDto.builder()
                .serviceId(11L)
                .serviceName("Service 2")
                .price(new BigDecimal("200.00"))
                .quantity(1)
                .build();

        ServiceOrderResourceRequestDto resource1 = ServiceOrderResourceRequestDto.builder()
                .resourceId(20L)
                .resourceName("Part 1")
                .price(new BigDecimal("50.00"))
                .quantity(2)
                .build();
        ServiceOrderResourceRequestDto resource2 = ServiceOrderResourceRequestDto.builder()
                .resourceId(21L)
                .resourceName("Part 2")
                .price(new BigDecimal("30.00"))
                .quantity(3)
                .build();

        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(customerId)
                .vehicleId(vehicleId)
                .services(List.of(item1, item2))
                .resources(List.of(resource1, resource2))
                .build();

        ArgumentCaptor<ServiceOrder> orderCaptor = ArgumentCaptor.forClass(ServiceOrder.class);
        when(gateway.insert(orderCaptor.capture())).thenAnswer(invocation -> {
            ServiceOrder order = invocation.getArgument(0);
            return order.withId(100L);
        });
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(requestDto);

        // Assert
        ServiceOrder capturedOrder = orderCaptor.getValue();
        // Services: 100 + 200 = 300
        // Resources: (2 * 50) + (3 * 30) = 100 + 90 = 190
        // Total: 300 + 190 = 490
        assertEquals(new BigDecimal("490.00"), capturedOrder.totalPrice());
    }

    @Test
    @DisplayName("Should create order with empty items and resources")
    void shouldCreateOrderWithEmptyItemsAndResources() {
        // Arrange
        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(1L)
                .vehicleId(2L)
                .description("Just inspection")
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.insert(any(ServiceOrder.class))).thenAnswer(invocation -> {
            ServiceOrder order = invocation.getArgument(0);
            return order.withId(100L);
        });

        // Act
        ServiceOrder result = useCase.execute(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.totalPrice());
        assertTrue(result.services().isEmpty());
        assertTrue(result.resources().isEmpty());
    }

    @Test
    @DisplayName("Should create order with null services and null resources lists")
    void shouldCreateOrderWithNullServicesAndResources() {
        // Arrange
        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(1L)
                .vehicleId(2L)
                .description("Null lists")
                .services(null)
                .resources(null)
                .build();

        when(gateway.insert(any(ServiceOrder.class))).thenAnswer(invocation -> {
            ServiceOrder order = invocation.getArgument(0);
            return order.withId(100L);
        });

        // Act
        ServiceOrder result = useCase.execute(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.totalPrice());
        assertTrue(result.services().isEmpty());
        assertTrue(result.resources().isEmpty());
        // No auto-advance (no items = no complete quote)
        assertEquals(ServiceOrderStatus.received().value(), result.status().value());
        verify(eventPublisher).publishOrderCreated(any(ServiceOrder.class));
        verify(eventPublisher, never()).publishOrderWaitingApproval(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Should handle null prices in items defaulting to zero")
    void shouldHandleNullPricesInItems() {
        // Arrange
        ServiceOrderItemRequestDto itemWithNullPrice = ServiceOrderItemRequestDto.builder()
                .serviceId(10L)
                .serviceName("Free inspection")
                .serviceDescription("Complimentary")
                .price(null)
                .quantity(1)
                .build();

        ServiceOrderResourceRequestDto resourceWithNullPrice = ServiceOrderResourceRequestDto.builder()
                .resourceId(20L)
                .resourceName("Free filter")
                .resourceDescription("Promo")
                .resourceType("PART")
                .price(null)
                .quantity(1)
                .build();

        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(1L)
                .vehicleId(2L)
                .description("Null prices")
                .services(List.of(itemWithNullPrice))
                .resources(List.of(resourceWithNullPrice))
                .build();

        when(gateway.insert(any(ServiceOrder.class))).thenAnswer(invocation -> {
            ServiceOrder order = invocation.getArgument(0);
            return order.withId(100L);
        });

        // Act
        ServiceOrder result = useCase.execute(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.totalPrice());
        assertEquals(1, result.services().size());
        assertEquals(1, result.resources().size());
        // No auto-advance (total == 0)
        assertEquals(ServiceOrderStatus.received().value(), result.status().value());
    }

    @Test
    @DisplayName("Should auto-advance with only services (no resources)")
    void shouldAutoAdvanceWithOnlyServices() {
        // Arrange
        ServiceOrderItemRequestDto item = ServiceOrderItemRequestDto.builder()
                .serviceId(10L)
                .serviceName("Oil Change")
                .serviceDescription("Full service")
                .price(new BigDecimal("200.00"))
                .quantity(1)
                .build();

        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(1L)
                .vehicleId(2L)
                .description("Only services")
                .services(List.of(item))
                .resources(List.of())
                .build();

        when(gateway.insert(any(ServiceOrder.class))).thenAnswer(invocation -> {
            ServiceOrder order = invocation.getArgument(0);
            return order.withId(100L);
        });
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(requestDto);

        // Assert — has items + total > 0 → auto-advance
        assertEquals(ServiceOrderStatus.waitingApproval().value(), result.status().value());
        verify(eventPublisher).publishOrderCreated(any(ServiceOrder.class));
        verify(eventPublisher).publishOrderWaitingApproval(any(ServiceOrder.class));
        verify(gateway, times(2)).update(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Should auto-advance with only resources (no services)")
    void shouldAutoAdvanceWithOnlyResources() {
        // Arrange
        ServiceOrderResourceRequestDto resource = ServiceOrderResourceRequestDto.builder()
                .resourceId(20L)
                .resourceName("Brake Pads")
                .resourceDescription("Set of 4")
                .resourceType("PART")
                .price(new BigDecimal("300.00"))
                .quantity(1)
                .build();

        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(1L)
                .vehicleId(2L)
                .description("Only resources")
                .services(List.of())
                .resources(List.of(resource))
                .build();

        when(gateway.insert(any(ServiceOrder.class))).thenAnswer(invocation -> {
            ServiceOrder order = invocation.getArgument(0);
            return order.withId(100L);
        });
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ServiceOrder result = useCase.execute(requestDto);

        // Assert — has items + total > 0 → auto-advance
        assertEquals(ServiceOrderStatus.waitingApproval().value(), result.status().value());
        verify(eventPublisher).publishOrderWaitingApproval(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Should NOT auto-advance when items exist but total is zero")
    void shouldNotAutoAdvanceWhenTotalIsZero() {
        // Arrange
        ServiceOrderItemRequestDto freeItem = ServiceOrderItemRequestDto.builder()
                .serviceId(10L)
                .serviceName("Free check")
                .serviceDescription("Courtesy")
                .price(BigDecimal.ZERO)
                .quantity(1)
                .build();

        ServiceOrderRequestDto requestDto = ServiceOrderRequestDto.builder()
                .customerId(1L)
                .vehicleId(2L)
                .description("Free items")
                .services(List.of(freeItem))
                .resources(List.of())
                .build();

        when(gateway.insert(any(ServiceOrder.class))).thenAnswer(invocation -> {
            ServiceOrder order = invocation.getArgument(0);
            return order.withId(100L);
        });

        // Act
        ServiceOrder result = useCase.execute(requestDto);

        // Assert — has items but total==0 → NO auto-advance
        assertEquals(ServiceOrderStatus.received().value(), result.status().value());
        verify(eventPublisher, never()).publishOrderWaitingApproval(any(ServiceOrder.class));
        verify(gateway, never()).update(any(ServiceOrder.class));
    }
}
