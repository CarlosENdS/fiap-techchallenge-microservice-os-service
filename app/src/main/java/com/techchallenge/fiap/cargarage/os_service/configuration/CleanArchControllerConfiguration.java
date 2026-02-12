package com.techchallenge.fiap.cargarage.os_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.techchallenge.fiap.cargarage.os_service.application.controller.ServiceOrderCleanArchController;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CancelServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CreateServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.FindServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.GetServiceOrderExecutionTimeUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.ProcessApprovalUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderStatusUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderUseCase;

/**
 * Clean Architecture controller configuration for dependency injection.
 */
@Configuration
public class CleanArchControllerConfiguration {

    @Bean
    public ServiceOrderCleanArchController serviceOrderCleanArchController(
            FindServiceOrderUseCase findServiceOrderUseCase,
            CreateServiceOrderUseCase createServiceOrderUseCase,
            UpdateServiceOrderUseCase updateServiceOrderUseCase,
            UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase,
            ProcessApprovalUseCase processApprovalUseCase,
            GetServiceOrderExecutionTimeUseCase getServiceOrderExecutionTimeUseCase,
            CancelServiceOrderUseCase cancelServiceOrderUseCase) {
        return new ServiceOrderCleanArchController(
                findServiceOrderUseCase,
                createServiceOrderUseCase,
                updateServiceOrderUseCase,
                updateServiceOrderStatusUseCase,
                processApprovalUseCase,
                getServiceOrderExecutionTimeUseCase,
                cancelServiceOrderUseCase);
    }
}
