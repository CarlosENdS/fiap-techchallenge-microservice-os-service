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
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderPersistenceDto;
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
}
