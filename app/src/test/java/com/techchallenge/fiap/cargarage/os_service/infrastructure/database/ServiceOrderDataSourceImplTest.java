package com.techchallenge.fiap.cargarage.os_service.infrastructure.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderPersistenceDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceRequestDto;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.database.entity.ServiceOrderItemEntity;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.database.entity.ServiceOrderResourceEntity;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.database.entity.ServiceOrderEntity;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.database.repository.ServiceOrderDataSourceImpl;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.database.repository.ServiceOrderRepository;

@ExtendWith(MockitoExtension.class)
class ServiceOrderDataSourceImplTest {

    @Mock
    private ServiceOrderRepository repository;

    private ServiceOrderDataSourceImpl dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new ServiceOrderDataSourceImpl(repository);
    }

    private ServiceOrderEntity createEntity(Long id) {
        ServiceOrderEntity entity = new ServiceOrderEntity();
        entity.setId(id);
        entity.setCustomerId(100L);
        entity.setCustomerName("Test Customer");
        entity.setVehicleId(200L);
        entity.setVehicleLicensePlate("ABC-1234");
        entity.setVehicleModel("Test Model");
        entity.setVehicleBrand("Test Brand");
        entity.setDescription("Sample complaint");
        entity.setStatus("RECEIVED");
        entity.setTotalPrice(new BigDecimal("500.00"));
        entity.setServices(new ArrayList<>());
        entity.setResources(new ArrayList<>());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    @Test
    @DisplayName("Should insert service order")
    void shouldInsertServiceOrder() {
        // Arrange
        Long generatedId = 1L;
        ServiceOrderPersistenceDto dto = ServiceOrderPersistenceDto.builder()
                .id(null)
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

        ServiceOrderEntity savedEntity = createEntity(generatedId);
        when(repository.save(any(ServiceOrderEntity.class))).thenReturn(savedEntity);

        // Act
        ServiceOrderDto result = dataSource.insert(dto);

        // Assert
        assertNotNull(result);
        assertEquals(generatedId, result.id());
    }

    @Test
    @DisplayName("Should find order by ID")
    void shouldFindOrderById() {
        // Arrange
        Long orderId = 1L;
        ServiceOrderEntity entity = createEntity(orderId);
        when(repository.findById(orderId)).thenReturn(Optional.of(entity));

        // Act
        Optional<ServiceOrderDto> result = dataSource.findById(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().id());
    }

    @Test
    @DisplayName("Should return empty when order not found")
    void shouldReturnEmptyWhenOrderNotFound() {
        // Arrange
        Long orderId = 1L;
        when(repository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Optional<ServiceOrderDto> result = dataSource.findById(orderId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all orders with pagination")
    void shouldFindAllOrdersWithPagination() {
        // Arrange
        PageRequestDto pageRequest = new PageRequestDto(0, 10);
        List<ServiceOrderEntity> entities = List.of(
                createEntity(1L),
                createEntity(2L));
        Page<ServiceOrderEntity> page = new PageImpl<>(entities);

        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        PageDto<ServiceOrderDto> result = dataSource.findAll(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.content().size());
    }

    @Test
    @DisplayName("Should find orders by customer ID")
    void shouldFindOrdersByCustomerId() {
        // Arrange
        Long customerId = 100L;
        PageRequestDto pageRequest = new PageRequestDto(0, 10);
        List<ServiceOrderEntity> entities = List.of(createEntity(1L));
        Page<ServiceOrderEntity> page = new PageImpl<>(entities);

        when(repository.findByCustomerId(eq(customerId), any(Pageable.class))).thenReturn(page);

        // Act
        PageDto<ServiceOrderDto> result = dataSource.findByCustomerId(customerId, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Should find orders by status")
    void shouldFindOrdersByStatus() {
        // Arrange
        String status = "IN_EXECUTION";
        PageRequestDto pageRequest = new PageRequestDto(0, 10);
        List<ServiceOrderEntity> entities = List.of(createEntity(1L));
        Page<ServiceOrderEntity> page = new PageImpl<>(entities);

        when(repository.findByStatus(eq(status), any(Pageable.class))).thenReturn(page);

        // Act
        PageDto<ServiceOrderDto> result = dataSource.findByStatus(status, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Should delete order by ID")
    void shouldDeleteOrderById() {
        // Arrange
        Long orderId = 1L;

        // Act
        dataSource.deleteById(orderId);

        // Assert
        verify(repository).deleteById(orderId);
    }

    @Test
    @DisplayName("Should update service order with services and resources")
    void shouldUpdateServiceOrderWithServicesAndResources() {
        Long orderId = 10L;
        ServiceOrderEntity existing = createEntity(orderId);
        existing.setServices(new ArrayList<>());
        existing.setResources(new ArrayList<>());

        ServiceOrderPersistenceDto request = ServiceOrderPersistenceDto.builder()
                .customerId(101L)
                .customerName("Updated Customer")
                .vehicleId(201L)
                .vehicleLicensePlate("XYZ-9999")
                .vehicleModel("Updated Model")
                .vehicleBrand("Updated Brand")
                .description("Updated complaint")
                .status("IN_DIAGNOSIS")
                .totalPrice(new BigDecimal("750.00"))
                .updatedAt(LocalDateTime.now())
                .services(List.of(ServiceOrderItemRequestDto.builder()
                        .serviceId(30L)
                        .serviceName("Alignment")
                        .serviceDescription("Wheel alignment")
                        .quantity(2)
                        .price(new BigDecimal("50.00"))
                        .build()))
                .resources(List.of(ServiceOrderResourceRequestDto.builder()
                        .resourceId(40L)
                        .resourceName("Filter")
                        .resourceDescription("Air filter")
                        .resourceType("PART")
                        .quantity(2)
                        .price(new BigDecimal("20.00"))
                        .build()))
                .build();

        when(repository.findById(orderId)).thenReturn(Optional.of(existing));
        when(repository.save(any(ServiceOrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrderDto result = dataSource.update(orderId, request);

        assertNotNull(result);
        assertEquals("IN_DIAGNOSIS", result.status());
        assertEquals(1, result.services().size());
        assertEquals(new BigDecimal("100.00"), result.services().get(0).totalPrice());
        assertEquals(1, result.resources().size());
        assertEquals(new BigDecimal("40.00"), result.resources().get(0).totalPrice());
    }

    @Test
    @DisplayName("Should update using zero totals when prices are null")
    void shouldUpdateWithZeroTotalWhenPriceIsNull() {
        Long orderId = 11L;
        ServiceOrderEntity existing = createEntity(orderId);
        existing.setServices(new ArrayList<>());
        existing.setResources(new ArrayList<>());

        ServiceOrderPersistenceDto request = ServiceOrderPersistenceDto.builder()
                .customerId(101L)
                .customerName("Updated Customer")
                .vehicleId(201L)
                .vehicleLicensePlate("XYZ-9999")
                .vehicleModel("Updated Model")
                .vehicleBrand("Updated Brand")
                .description("Updated complaint")
                .status("IN_DIAGNOSIS")
                .totalPrice(new BigDecimal("0.00"))
                .updatedAt(null)
                .services(List.of(ServiceOrderItemRequestDto.builder()
                        .serviceId(31L)
                        .serviceName("Test")
                        .serviceDescription("Test")
                        .quantity(1)
                        .price(null)
                        .build()))
                .resources(List.of(ServiceOrderResourceRequestDto.builder()
                        .resourceId(41L)
                        .resourceName("Test")
                        .resourceDescription("Test")
                        .resourceType("PART")
                        .quantity(1)
                        .price(null)
                        .build()))
                .build();

        when(repository.findById(orderId)).thenReturn(Optional.of(existing));
        when(repository.save(any(ServiceOrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrderDto result = dataSource.update(orderId, request);

        assertNotNull(result.updatedAt());
        assertEquals(BigDecimal.ZERO, result.services().get(0).totalPrice());
        assertEquals(BigDecimal.ZERO, result.resources().get(0).totalPrice());
    }

    @Test
    @DisplayName("Should throw when updating non existing service order")
    void shouldThrowWhenUpdatingNonExistingServiceOrder() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dataSource.update(999L, ServiceOrderPersistenceDto.builder()
                        .customerId(1L)
                        .vehicleId(1L)
                        .services(List.of())
                        .resources(List.of())
                        .build()));

        assertEquals("Service order not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should return empty page when status is invalid")
    void shouldReturnEmptyPageWhenStatusInvalid() {
        PageDto<ServiceOrderDto> result = dataSource.findByStatus("INVALID", new PageRequestDto(0, 10));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(repository, never()).findByStatus(any(), any());
    }

    @Test
    @DisplayName("Should return empty page when status is null")
    void shouldReturnEmptyPageWhenStatusNull() {
        PageDto<ServiceOrderDto> result = dataSource.findByStatus(null, new PageRequestDto(1, 5));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(1, result.pageNumber());
        assertEquals(5, result.pageSize());
        verify(repository, never()).findByStatus(any(), any());
    }

    @Test
    @DisplayName("Should map services and resources when finding by id")
    void shouldMapServicesAndResourcesWhenFindingById() {
        ServiceOrderEntity entity = createEntity(15L);

        ServiceOrderItemEntity itemEntity = new ServiceOrderItemEntity();
        itemEntity.setId(1L);
        itemEntity.setOrder(entity);
        itemEntity.setServiceId(55L);
        itemEntity.setServiceName("Inspection");
        itemEntity.setServiceDescription("General inspection");
        itemEntity.setQuantity(1);
        itemEntity.setPrice(new BigDecimal("80.00"));
        itemEntity.setTotalPrice(new BigDecimal("80.00"));

        ServiceOrderResourceEntity resourceEntity = new ServiceOrderResourceEntity();
        resourceEntity.setId(2L);
        resourceEntity.setOrder(entity);
        resourceEntity.setResourceId(66L);
        resourceEntity.setResourceName("Spark plug");
        resourceEntity.setResourceDescription("New spark plug");
        resourceEntity.setResourceType("PART");
        resourceEntity.setQuantity(1);
        resourceEntity.setPrice(new BigDecimal("30.00"));
        resourceEntity.setTotalPrice(new BigDecimal("30.00"));

        entity.setServices(new ArrayList<>(List.of(itemEntity)));
        entity.setResources(new ArrayList<>(List.of(resourceEntity)));

        when(repository.findById(15L)).thenReturn(Optional.of(entity));

        Optional<ServiceOrderDto> result = dataSource.findById(15L);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().services().size());
        assertEquals(1, result.get().resources().size());
        assertEquals("Inspection", result.get().services().get(0).serviceName());
        assertEquals("Spark plug", result.get().resources().get(0).resourceName());
    }

    @Test
    @DisplayName("Should set createdAt when inserting without createdAt")
    void shouldSetCreatedAtWhenInsertingWithoutCreatedAt() {
        ServiceOrderPersistenceDto dto = ServiceOrderPersistenceDto.builder()
                .customerId(100L)
                .customerName("Test")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Model")
                .vehicleBrand("Brand")
                .description("Desc")
                .status("RECEIVED")
                .totalPrice(new BigDecimal("10.00"))
                .services(List.of(ServiceOrderItemRequestDto.builder()
                        .serviceId(1L)
                        .serviceName("S")
                        .serviceDescription("D")
                        .quantity(1)
                        .price(null)
                        .build()))
                .resources(List.of(ServiceOrderResourceRequestDto.builder()
                        .resourceId(2L)
                        .resourceName("R")
                        .resourceDescription("RD")
                        .resourceType("PART")
                        .quantity(1)
                        .price(null)
                        .build()))
                .build();

        when(repository.save(any(ServiceOrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrderDto result = dataSource.insert(dto);

        assertNotNull(result.createdAt());
        assertEquals(BigDecimal.ZERO, result.services().get(0).totalPrice());
        assertEquals(BigDecimal.ZERO, result.resources().get(0).totalPrice());
    }
}
