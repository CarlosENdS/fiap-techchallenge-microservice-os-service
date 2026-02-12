package com.techchallenge.fiap.cargarage.os_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CancelServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CreateServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.FindServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.GetServiceOrderExecutionTimeUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.ProcessApprovalUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderStatusUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging.ServiceOrderEventPublisher;

/**
 * Use case configuration for dependency injection.
 */
@Configuration
public class UseCaseConfiguration {

    @Bean
    public FindServiceOrderUseCase findServiceOrderUseCase(ServiceOrderGateway gateway) {
        return new FindServiceOrderUseCase(gateway);
    }

    @Bean
    public CreateServiceOrderUseCase createServiceOrderUseCase(
            ServiceOrderGateway gateway,
            ServiceOrderEventPublisher eventPublisher) {
        return new CreateServiceOrderUseCase(gateway, eventPublisher);
    }

    @Bean
    public UpdateServiceOrderUseCase updateServiceOrderUseCase(ServiceOrderGateway gateway) {
        return new UpdateServiceOrderUseCase(gateway);
    }

    @Bean
    public UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase(
            ServiceOrderGateway gateway,
            ServiceOrderEventPublisher eventPublisher) {
        return new UpdateServiceOrderStatusUseCase(gateway, eventPublisher);
    }

    @Bean
    public ProcessApprovalUseCase processApprovalUseCase(
            ServiceOrderGateway gateway,
            ServiceOrderEventPublisher eventPublisher) {
        return new ProcessApprovalUseCase(gateway, eventPublisher);
    }

    @Bean
    public GetServiceOrderExecutionTimeUseCase getServiceOrderExecutionTimeUseCase(
            ServiceOrderGateway gateway) {
        return new GetServiceOrderExecutionTimeUseCase(gateway);
    }

    @Bean
    public CancelServiceOrderUseCase cancelServiceOrderUseCase(
            ServiceOrderGateway gateway,
            ServiceOrderEventPublisher eventPublisher) {
        return new CancelServiceOrderUseCase(gateway, eventPublisher);
    }
}
