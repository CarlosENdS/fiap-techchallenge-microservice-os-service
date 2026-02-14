package com.techchallenge.fiap.cargarage.os_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.application.interfaces.ServiceOrderDataSource;

/**
 * Gateway configuration for dependency injection.
 */
@Configuration
public class GatewayConfiguration {

    @Bean
    public ServiceOrderGateway serviceOrderGateway(ServiceOrderDataSource dataSource) {
        return new ServiceOrderGateway(dataSource);
    }
}
