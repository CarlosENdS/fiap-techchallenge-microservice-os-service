package com.techchallenge.fiap.cargarage.os_service.infrastructure.database.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.techchallenge.fiap.cargarage.os_service.application.dto.PageDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.PageRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderPersistenceDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.enums.ServiceOrderStatusEnum;
import com.techchallenge.fiap.cargarage.os_service.application.interfaces.ServiceOrderDataSource;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.database.entity.ServiceOrderEntity;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.database.entity.ServiceOrderItemEntity;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.database.entity.ServiceOrderResourceEntity;

/**
 * Implementation of ServiceOrderDataSource using JPA.
 */
@Component
@Transactional
public class ServiceOrderDataSourceImpl implements ServiceOrderDataSource {

    private final ServiceOrderRepository serviceOrderRepository;

    public ServiceOrderDataSourceImpl(ServiceOrderRepository serviceOrderRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
    }

    @Override
    public ServiceOrderDto insert(ServiceOrderPersistenceDto requestDto) {
        ServiceOrderEntity entity = toEntity(requestDto);
        if (requestDto.createdAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        ServiceOrderEntity saved = serviceOrderRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ServiceOrderDto update(Long id, ServiceOrderPersistenceDto requestDto) {
        ServiceOrderEntity existing = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service order not found"));

        // Clear existing services and resources
        existing.getServices().clear();
        existing.getResources().clear();

        // Update fields
        existing.setCustomerId(requestDto.customerId());
        existing.setCustomerName(requestDto.customerName());
        existing.setVehicleId(requestDto.vehicleId());
        existing.setVehicleLicensePlate(requestDto.vehicleLicensePlate());
        existing.setVehicleModel(requestDto.vehicleModel());
        existing.setVehicleBrand(requestDto.vehicleBrand());
        existing.setDescription(requestDto.description());
        existing.setStatus(requestDto.status());
        existing.setTotalPrice(requestDto.totalPrice());
        existing.setUpdatedAt(
                requestDto.updatedAt() != null ? requestDto.updatedAt() : LocalDateTime.now());
        existing.setApprovedAt(requestDto.approvedAt());
        existing.setFinishedAt(requestDto.finishedAt());
        existing.setDeliveredAt(requestDto.deliveredAt());

        // Add new services
        if (requestDto.services() != null) {
            for (ServiceOrderItemRequestDto serviceDto : requestDto.services()) {
                ServiceOrderItemEntity itemEntity = new ServiceOrderItemEntity();
                itemEntity.setOrder(existing);
                itemEntity.setServiceId(serviceDto.serviceId());
                itemEntity.setServiceName(serviceDto.serviceName());
                itemEntity.setServiceDescription(serviceDto.serviceDescription());
                itemEntity.setQuantity(serviceDto.quantity());
                itemEntity.setPrice(serviceDto.price());
                BigDecimal totalPrice = serviceDto.price() != null
                        ? serviceDto.price().multiply(BigDecimal.valueOf(serviceDto.quantity()))
                        : BigDecimal.ZERO;
                itemEntity.setTotalPrice(totalPrice);
                existing.getServices().add(itemEntity);
            }
        }

        // Add new resources
        if (requestDto.resources() != null) {
            for (ServiceOrderResourceRequestDto resourceDto : requestDto.resources()) {
                ServiceOrderResourceEntity resourceEntity = new ServiceOrderResourceEntity();
                resourceEntity.setOrder(existing);
                resourceEntity.setResourceId(resourceDto.resourceId());
                resourceEntity.setResourceName(resourceDto.resourceName());
                resourceEntity.setResourceDescription(resourceDto.resourceDescription());
                resourceEntity.setResourceType(resourceDto.resourceType());
                resourceEntity.setQuantity(resourceDto.quantity());
                resourceEntity.setPrice(resourceDto.price());
                BigDecimal totalPrice = resourceDto.price() != null
                        ? resourceDto.price().multiply(BigDecimal.valueOf(resourceDto.quantity()))
                        : BigDecimal.ZERO;
                resourceEntity.setTotalPrice(totalPrice);
                existing.getResources().add(resourceEntity);
            }
        }

        ServiceOrderEntity saved = serviceOrderRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public Optional<ServiceOrderDto> findById(Long id) {
        return serviceOrderRepository.findById(id).map(this::toDto);
    }

    @Override
    public PageDto<ServiceOrderDto> findAll(PageRequestDto pageRequest) {
        Pageable pageable = PageRequest.of(pageRequest.page(), pageRequest.size());
        Page<ServiceOrderEntity> page = serviceOrderRepository.findAll(pageable);
        List<ServiceOrderDto> dtos = page.stream().map(this::toDto).toList();
        return new PageDto<>(dtos, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public PageDto<ServiceOrderDto> findByCustomerId(
            Long customerId,
            PageRequestDto pageRequest) {
        Pageable pageable = PageRequest.of(pageRequest.page(), pageRequest.size());
        Page<ServiceOrderEntity> page = serviceOrderRepository.findByCustomerId(customerId, pageable);
        List<ServiceOrderDto> dtos = page.stream().map(this::toDto).toList();
        return new PageDto<>(dtos, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public PageDto<ServiceOrderDto> findByStatus(String status, PageRequestDto pageRequest) {
        ServiceOrderStatusEnum enumVal = ServiceOrderStatusEnum.fromString(
                status == null ? "" : status);
        Pageable pageable = PageRequest.of(pageRequest.page(), pageRequest.size());
        if (enumVal == null) {
            return new PageDto<>(List.of(), 0, pageable.getPageNumber(), pageable.getPageSize());
        }
        Page<ServiceOrderEntity> page = serviceOrderRepository.findByStatus(enumVal.name(), pageable);
        List<ServiceOrderDto> dtos = page.stream().map(this::toDto).toList();
        return new PageDto<>(dtos, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public void deleteById(Long id) {
        serviceOrderRepository.deleteById(id);
    }

    private ServiceOrderEntity toEntity(ServiceOrderPersistenceDto dto) {
        ServiceOrderEntity entity = ServiceOrderEntity.builder()
                .customerId(dto.customerId())
                .customerName(dto.customerName())
                .vehicleId(dto.vehicleId())
                .vehicleLicensePlate(dto.vehicleLicensePlate())
                .vehicleModel(dto.vehicleModel())
                .vehicleBrand(dto.vehicleBrand())
                .description(dto.description())
                .status(dto.status())
                .totalPrice(dto.totalPrice())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .approvedAt(dto.approvedAt())
                .finishedAt(dto.finishedAt())
                .deliveredAt(dto.deliveredAt())
                .build();

        if (dto.services() != null) {
            for (ServiceOrderItemRequestDto serviceDto : dto.services()) {
                ServiceOrderItemEntity itemEntity = new ServiceOrderItemEntity();
                itemEntity.setOrder(entity);
                itemEntity.setServiceId(serviceDto.serviceId());
                itemEntity.setServiceName(serviceDto.serviceName());
                itemEntity.setServiceDescription(serviceDto.serviceDescription());
                itemEntity.setQuantity(serviceDto.quantity());
                itemEntity.setPrice(serviceDto.price());
                BigDecimal totalPrice = serviceDto.price() != null
                        ? serviceDto.price().multiply(BigDecimal.valueOf(serviceDto.quantity()))
                        : BigDecimal.ZERO;
                itemEntity.setTotalPrice(totalPrice);
                entity.getServices().add(itemEntity);
            }
        }

        if (dto.resources() != null) {
            for (ServiceOrderResourceRequestDto resourceDto : dto.resources()) {
                ServiceOrderResourceEntity resourceEntity = new ServiceOrderResourceEntity();
                resourceEntity.setOrder(entity);
                resourceEntity.setResourceId(resourceDto.resourceId());
                resourceEntity.setResourceName(resourceDto.resourceName());
                resourceEntity.setResourceDescription(resourceDto.resourceDescription());
                resourceEntity.setResourceType(resourceDto.resourceType());
                resourceEntity.setQuantity(resourceDto.quantity());
                resourceEntity.setPrice(resourceDto.price());
                BigDecimal totalPrice = resourceDto.price() != null
                        ? resourceDto.price().multiply(BigDecimal.valueOf(resourceDto.quantity()))
                        : BigDecimal.ZERO;
                resourceEntity.setTotalPrice(totalPrice);
                entity.getResources().add(resourceEntity);
            }
        }

        return entity;
    }

    private ServiceOrderDto toDto(ServiceOrderEntity entity) {
        List<ServiceOrderItemDto> services = entity.getServices().stream()
                .map(s -> ServiceOrderItemDto.builder()
                        .id(s.getId())
                        .serviceId(s.getServiceId())
                        .serviceName(s.getServiceName())
                        .serviceDescription(s.getServiceDescription())
                        .price(s.getPrice())
                        .quantity(s.getQuantity())
                        .totalPrice(s.getTotalPrice())
                        .build())
                .toList();

        List<ServiceOrderResourceDto> resources = entity.getResources().stream()
                .map(r -> ServiceOrderResourceDto.builder()
                        .id(r.getId())
                        .resourceId(r.getResourceId())
                        .resourceName(r.getResourceName())
                        .resourceDescription(r.getResourceDescription())
                        .resourceType(r.getResourceType())
                        .price(r.getPrice())
                        .quantity(r.getQuantity())
                        .totalPrice(r.getTotalPrice())
                        .build())
                .toList();

        return ServiceOrderDto.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .customerName(entity.getCustomerName())
                .vehicleId(entity.getVehicleId())
                .vehicleLicensePlate(entity.getVehicleLicensePlate())
                .vehicleModel(entity.getVehicleModel())
                .vehicleBrand(entity.getVehicleBrand())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .totalPrice(entity.getTotalPrice())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .approvedAt(entity.getApprovedAt())
                .finishedAt(entity.getFinishedAt())
                .deliveredAt(entity.getDeliveredAt())
                .services(services)
                .resources(resources)
                .build();
    }
}
